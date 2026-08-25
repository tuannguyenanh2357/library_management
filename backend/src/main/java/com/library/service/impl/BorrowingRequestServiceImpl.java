package com.library.service.impl;

import com.library.dto.request.BorrowingRequestApprovalRequest;
import com.library.dto.request.BorrowingRequestCreationRequest;
import com.library.dto.request.BorrowingCreationRequest;
import com.library.dto.response.BorrowingRequestResponse;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BorrowingRequest;
import com.library.entity.Member;
import com.library.entity.enums.BookCopyStatus;
import com.library.entity.enums.BorrowingRequestStatus;
import com.library.entity.enums.FineStatus;
import com.library.mapper.BorrowingRequestMapper;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.BorrowingRequestRepository;
import com.library.repository.FineRepository;
import com.library.repository.MemberRepository;
import com.library.service.interfaces.BorrowingRequestService;
import com.library.service.interfaces.BorrowingService;
import com.library.config.RabbitMQConfig;
import com.library.dto.event.BorrowingRequestApprovedEvent;
import com.library.dto.event.BorrowingRequestRejectedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import lombok.extern.slf4j.Slf4j;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.exception.MemberNotFoundException;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BorrowingRequestServiceImpl implements BorrowingRequestService {

        BorrowingRequestRepository requestRepository;
        MemberRepository memberRepository;
        BookRepository bookRepository;
        BookCopyRepository bookCopyRepository;
        FineRepository fineRepository;
        BorrowingService borrowingService;
        BorrowingRequestMapper mapper;
        RabbitTemplate rabbitTemplate;

        @Override
        @Transactional
        public BorrowingRequestResponse createRequest(BorrowingRequestCreationRequest requestt) {
                Member member = memberRepository.findById(requestt.getMemberId())
                                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));

                if (member.hasOverdueBorrowings()) {
                        throw new AppException(ErrorCode.HAS_OVERDUE_BOOKS,
                                        "Bạn không thể gửi yêu cầu mượn mới khi đang có sách quá hạn chưa trả");
                }
                if (fineRepository.existsByBorrowingMemberIdAndStatus(member.getId(), FineStatus.UNPAID)) {
                        throw new AppException(ErrorCode.HAS_UNPAID_FINES,
                                        "Bạn không thể gửi yêu cầu mượn mới khi đang có khoản phạt chưa thanh toán");
                }

                Book book = bookRepository.findById(requestt.getBookId())
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_NOT_FOUND,
                                                "Không tìm thấy đầu sách"));

                BorrowingRequest request = BorrowingRequest.builder()
                                .member(member)
                                .book(book)
                                .status(BorrowingRequestStatus.PENDING)
                                .requestDate(LocalDateTime.now())
                                .expectedDueDate(requestt.getExpectedDueDate())
                                .notes(requestt.getNotes())
                                .build();

                BorrowingRequest saved = requestRepository.save(request);
                long availableCopiesCount = bookCopyRepository.countByBookIdAndStatus(book.getId(),
                                BookCopyStatus.AVAILABLE);
                return mapper.toResponse(saved, availableCopiesCount);
        }

        // Lấy danh sách các Yêu cầu mượn sách đang chờ duyệt (PENDING)
        @Override
        public List<BorrowingRequestResponse> getPendingRequests() {
                return requestRepository.findByStatusWithRelations(BorrowingRequestStatus.PENDING)
                                .stream()
                                .map(r -> mapper.toResponse(r,
                                                bookCopyRepository.countByBookIdAndStatus(r.getBook().getId(),
                                                                BookCopyStatus.AVAILABLE)))
                                .collect(Collectors.toList());
        }

        // Lấy danh sách các Yêu cầu mượn sách đã được duyệt - đang chờ độc giả đến lấy
        // (APPROVED)
        @Override
        public List<BorrowingRequestResponse> getApprovedRequests() {
                return requestRepository.findByStatusWithRelations(BorrowingRequestStatus.APPROVED)
                                .stream()
                                .map(r -> mapper.toResponse(r,
                                                bookCopyRepository.countByBookIdAndStatus(r.getBook().getId(),
                                                                BookCopyStatus.AVAILABLE)))
                                .collect(Collectors.toList());
        }

        @Override
        public List<BorrowingRequestResponse> getHistoryRequests() {
                return requestRepository.findHistoryWithRelations()
                                .stream()
                                .map(r -> mapper.toResponse(r,
                                                bookCopyRepository.countByBookIdAndStatus(r.getBook().getId(),
                                                                BookCopyStatus.AVAILABLE)))
                                .collect(Collectors.toList());
        }

        // member xem danh sách yêu cầu mượn sách
        @Override
        public List<BorrowingRequestResponse> getRequestsByMember(Long memberId) {
                return requestRepository.findByMemberIdWithRelations(memberId)
                                .stream()
                                .map(r -> mapper.toResponse(r,
                                                bookCopyRepository.countByBookIdAndStatus(r.getBook().getId(),
                                                                BookCopyStatus.AVAILABLE)))
                                .collect(Collectors.toList());
        }

        /**
         * Bước 1 - DUYỆT: Admin xem xét và duyệt yêu cầu.
         * Hệ thống sẽ giữ chỗ 1 BookCopy (RESERVED) và lưu ID đó vào yêu cầu.
         * Chưa tạo Borrowing - chờ độc giả đến lấy sách thực tế.
         */
        @Override
        @Transactional
        public BorrowingRequestResponse approveRequest(Long requestId,
                        BorrowingRequestApprovalRequest approvalRequest) {
                BorrowingRequest request = requestRepository.findById(requestId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Yêu cầu không tồn tại"));

                if (request.getStatus() != BorrowingRequestStatus.PENDING) {
                        throw new AppException(ErrorCode.INVALID_REQUEST,
                                        "Chỉ có thể phê duyệt yêu cầu đang chờ xử lý");
                }

                // Tìm và giữ chỗ bản sao sách đầu tiên có sẵn
                BookCopy bookCopy = bookCopyRepository
                                .findFirstByBook_IdAndStatus(request.getBook().getId(), BookCopyStatus.AVAILABLE)
                                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_AVAILABLE,
                                                "Không còn cuốn sách nào khả dụng trong kho"));

                // Đặt sách về trạng thái RESERVED - người khác không thể mượn cuốn này
                bookCopy.setStatus(BookCopyStatus.RESERVED);
                bookCopyRepository.save(bookCopy);

                // Lưu ID cuốn sách đã giữ chỗ và thời điểm duyệt vào yêu cầu
                request.setAssignedBookCopyId(bookCopy.getId());
                request.setApprovedDate(LocalDateTime.now());
                request.setStatus(BorrowingRequestStatus.APPROVED);
                request.setProcessedDate(LocalDateTime.now());

                long count = bookCopyRepository.countByBookIdAndStatus(request.getBook().getId(),
                                BookCopyStatus.AVAILABLE);
                BorrowingRequest savedRequest = requestRepository.save(request);
                BorrowingRequestResponse response = mapper.toResponse(savedRequest, count);

                try {
                        BorrowingRequestApprovedEvent event = BorrowingRequestApprovedEvent.builder()
                                        .requestId(savedRequest.getId())
                                        .memberEmail(savedRequest.getMember().getEmail())
                                        .memberName(savedRequest.getMember().getName())
                                        .bookTitle(savedRequest.getBook().getTitle())
                                        .expectedDueDate(savedRequest.getExpectedDueDate())
                                        .build();

                        rabbitTemplate.convertAndSend(
                                        RabbitMQConfig.RESERVATION_EXCHANGE,
                                        RabbitMQConfig.REQUEST_APPROVED_ROUTING_KEY, event);
                        log.info("[RABBITMQ PRODUCER] Đã gửi BorrowingRequestApprovedEvent cho yêu cầu ID: {}",
                                        savedRequest.getId());
                } catch (Exception e) {
                        log.error("Lỗi khi gửi sự kiện phê duyệt qua RabbitMQ: {}", e.getMessage());
                }

                return response;
        }

        /**
         * Bước 2 - GIAO SÁCH: Độc giả đến thư viện lấy sách, Admin xác nhận giao.
         * Lúc này mới thực sự tạo Borrowing - ngày mượn tính từ hôm nay.
         */
        @Override
        @Transactional
        public BorrowingRequestResponse issueBook(Long requestId) {
                BorrowingRequest request = requestRepository.findById(requestId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Yêu cầu không tồn tại"));

                if (request.getStatus() != BorrowingRequestStatus.APPROVED) {
                        throw new AppException(ErrorCode.INVALID_REQUEST,
                                        "Chỉ có thể giao sách cho yêu cầu đã được duyệt và đang chờ lấy sách");
                }

                if (request.getAssignedBookCopyId() == null) {
                        throw new AppException(ErrorCode.INVALID_REQUEST,
                                        "Yêu cầu này chưa được gán bản sao sách nào");
                }

                // Tạm thời đặt BookCopy về AVAILABLE để hàm borrowBook() có thể xử lý mượn bình
                // thường
                bookCopyRepository.findById(request.getAssignedBookCopyId()).ifPresent(copy -> {
                        copy.setStatus(BookCopyStatus.AVAILABLE);
                        bookCopyRepository.save(copy);
                });

                // Tạo phiếu mượn thực tế - ngày bắt đầu tính từ hôm nay
                BorrowingCreationRequest borrowingDto = new BorrowingCreationRequest(
                                request.getMember().getId(),
                                request.getAssignedBookCopyId(),
                                request.getExpectedDueDate(),
                                request.getNotes());
                borrowingService.borrowBook(borrowingDto);

                // Cập nhật yêu cầu thành COMPLETED
                request.setStatus(BorrowingRequestStatus.COMPLETED);
                request.setProcessedDate(LocalDateTime.now());

                long count = bookCopyRepository.countByBookIdAndStatus(request.getBook().getId(),
                                BookCopyStatus.AVAILABLE);
                return mapper.toResponse(requestRepository.save(request), count);
        }

        /**
         * Hủy yêu cầu đã duyệt do độc giả không đến lấy sách.
         * Trả sách về kho (AVAILABLE) và đánh dấu yêu cầu là EXPIRED.
         */
        @Override
        @Transactional
        public BorrowingRequestResponse expireRequest(Long requestId) {
                BorrowingRequest request = requestRepository.findById(requestId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Yêu cầu không tồn tại"));

                if (request.getStatus() != BorrowingRequestStatus.APPROVED) {
                        throw new AppException(ErrorCode.INVALID_REQUEST,
                                        "Chỉ có thể hủy các yêu cầu đang ở trạng thái chờ lấy sách");
                }

                // Trả sách về kho
                if (request.getAssignedBookCopyId() != null) {
                        bookCopyRepository.findById(request.getAssignedBookCopyId()).ifPresent(copy -> {
                                copy.setStatus(BookCopyStatus.AVAILABLE);
                                bookCopyRepository.save(copy);
                        });
                }

                request.setStatus(BorrowingRequestStatus.EXPIRED);
                request.setProcessedDate(LocalDateTime.now());

                long count = bookCopyRepository.countByBookIdAndStatus(request.getBook().getId(),
                                BookCopyStatus.AVAILABLE);
                return mapper.toResponse(requestRepository.save(request), count);
        }

        @Override
        @Transactional
        public BorrowingRequestResponse rejectRequest(Long requestId, String reason) {
                BorrowingRequest request = requestRepository.findById(requestId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Yêu cầu không tồn tại"));

                if (request.getStatus() != BorrowingRequestStatus.PENDING) {
                        throw new AppException(ErrorCode.INVALID_REQUEST, "Chỉ có thể từ chối yêu cầu đang chờ xử lý");
                }

                request.setStatus(BorrowingRequestStatus.REJECTED);
                request.setProcessedDate(LocalDateTime.now());
                request.setNotes(reason);

                long count = bookCopyRepository.countByBookIdAndStatus(request.getBook().getId(),
                                BookCopyStatus.AVAILABLE);
                BorrowingRequest savedRequest = requestRepository.save(request);
                BorrowingRequestResponse response = mapper.toResponse(savedRequest, count);

                try {
                        BorrowingRequestRejectedEvent event = BorrowingRequestRejectedEvent.builder()
                                        .requestId(savedRequest.getId())
                                        .memberEmail(savedRequest.getMember().getEmail())
                                        .memberName(savedRequest.getMember().getName())
                                        .bookTitle(savedRequest.getBook().getTitle())
                                        .reason(reason)
                                        .build();

                        rabbitTemplate.convertAndSend(
                                        RabbitMQConfig.RESERVATION_EXCHANGE,
                                        RabbitMQConfig.REQUEST_REJECTED_ROUTING_KEY, event);
                        log.info("[RABBITMQ PRODUCER] Đã gửi BorrowingRequestRejectedEvent cho yêu cầu ID: {}",
                                        savedRequest.getId());
                } catch (Exception e) {
                        log.error("Lỗi khi gửi sự kiện từ chối qua RabbitMQ: {}", e.getMessage());
                }

                return response;
        }

        @Override
        @Transactional
        public BorrowingRequestResponse cancelRequest(Long requestId, String username) {
                BorrowingRequest request = requestRepository.findById(requestId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Không tìm thấy yêu cầu"));

                if (!request.getMember().getUsername().equals(username)) {
                        throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền hủy yêu cầu này");
                }
                if (request.getStatus() != BorrowingRequestStatus.PENDING) {
                        throw new AppException(ErrorCode.INVALID_REQUEST, "Chỉ có thể hủy yêu cầu đang chờ duyệt");
                }

                request.setStatus(BorrowingRequestStatus.CANCELLED);
                request.setProcessedDate(LocalDateTime.now());

                long count = bookCopyRepository.countByBookIdAndStatus(request.getBook().getId(),
                                BookCopyStatus.AVAILABLE);
                return mapper.toResponse(requestRepository.save(request), count);
        }
}
