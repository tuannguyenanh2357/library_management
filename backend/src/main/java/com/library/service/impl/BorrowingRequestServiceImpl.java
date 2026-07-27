package com.library.service.impl;

import com.library.dto.request.BorrowingRequestApprovalRequest;
import com.library.dto.request.BorrowingRequestCreationRequest;
import com.library.dto.request.BorrowingCreationRequest;
import com.library.dto.response.BorrowingRequestResponse;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BorrowingRequest;
import com.library.entity.Member;
import com.library.entity.enums.BorrowingRequestStatus;
import com.library.mapper.BorrowingRequestMapper;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.BorrowingRequestRepository;
import com.library.repository.FineRepository;
import com.library.repository.MemberRepository;
import com.library.service.interfaces.BorrowingRequestService;
import com.library.service.interfaces.BorrowingService;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.exception.MemberNotFoundException;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import com.library.entity.enums.BookCopyStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class BorrowingRequestServiceImpl implements BorrowingRequestService {

        final BorrowingRequestRepository requestRepository;
        final MemberRepository memberRepository;
        final BookRepository bookRepository;
        final BookCopyRepository bookCopyRepository;
        final FineRepository fineRepository;
        final BorrowingService borrowingService;
        final BorrowingRequestMapper mapper;

        @Override
        public BorrowingRequestResponse createRequest(BorrowingRequestCreationRequest dto) {
                Member member = memberRepository.findById(dto.getMemberId())
                                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));

                if (member.hasOverdueBorrowings()) {
                        throw new AppException(ErrorCode.HAS_OVERDUE_BOOKS,
                                        "Bạn không thể gửi yêu cầu mượn mới khi đang có sách quá hạn chưa trả");
                }
                if (fineRepository.existsByBorrowing_Member_IdAndStatus(member.getId(),
                                com.library.entity.enums.FineStatus.UNPAID)) {
                        throw new AppException(ErrorCode.HAS_UNPAID_FINES,
                                        "Bạn không thể gửi yêu cầu mượn mới khi đang có khoản phạt chưa thanh toán");
                }

                Book book = bookRepository.findById(dto.getBookId())
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_NOT_FOUND,
                                                "Không tìm thấy đầu sách"));

                BorrowingRequest request = BorrowingRequest.builder()
                                .member(member)
                                .book(book)
                                .status(BorrowingRequestStatus.PENDING)
                                .requestDate(LocalDateTime.now())
                                .expectedDueDate(dto.getExpectedDueDate())
                                .notes(dto.getNotes())
                                .build();

                BorrowingRequest saved = requestRepository.save(request);
                long count = bookCopyRepository.countByBook_IdAndStatus(book.getId(), BookCopyStatus.AVAILABLE);
                return mapper.toResponse(saved, count);
        }

        @Override
        public List<BorrowingRequestResponse> getPendingRequests() {
                return requestRepository.findByStatusWithRelations(BorrowingRequestStatus.PENDING)
                                .stream()
                                .map(r -> mapper.toResponse(r,
                                                bookCopyRepository.countByBook_IdAndStatus(r.getBook().getId(),
                                                                BookCopyStatus.AVAILABLE)))
                                .collect(Collectors.toList());
        }

        @Override
        public List<BorrowingRequestResponse> getHistoryRequests() {
                return requestRepository.findHistoryWithRelations()
                                .stream()
                                .map(r -> mapper.toResponse(r,
                                                bookCopyRepository.countByBook_IdAndStatus(r.getBook().getId(),
                                                                BookCopyStatus.AVAILABLE)))
                                .collect(Collectors.toList());
        }

        @Override
        public List<BorrowingRequestResponse> getRequestsByMember(Long memberId) {
                return requestRepository.findByMemberIdWithRelations(memberId)
                                .stream()
                                .map(r -> mapper.toResponse(r,
                                                bookCopyRepository.countByBook_IdAndStatus(r.getBook().getId(),
                                                                BookCopyStatus.AVAILABLE)))
                                .collect(Collectors.toList());
        }

        @Override
        public BorrowingRequestResponse approveRequest(Long requestId,
                        BorrowingRequestApprovalRequest approvalRequest) {
                BorrowingRequest request = requestRepository.findById(requestId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Yêu cầu không tồn tại"));

                if (request.getStatus() != BorrowingRequestStatus.PENDING) {
                        throw new AppException(ErrorCode.INVALID_REQUEST,
                                        "Chỉ có thể phê duyệt yêu cầu đang chờ xử lý");
                }

                // gán bản sao đầu tiên có sẵn
                BookCopy bookCopy = bookCopyRepository
                                .findFirstByBook_IdAndStatus(request.getBook().getId(), BookCopyStatus.AVAILABLE)
                                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_AVAILABLE,
                                                "Không còn cuốn sách nào khả dụng trong kho"));

                // Sử dụng ngày dự kiến trả và ghi chú từ yêu cầu
                BorrowingCreationRequest borrowingDto = new BorrowingCreationRequest(
                                request.getMember().getId(),
                                bookCopy.getId(),
                                request.getExpectedDueDate(),
                                request.getNotes());
                borrowingService.borrowBook(borrowingDto);

                request.setStatus(BorrowingRequestStatus.APPROVED);
                request.setProcessedDate(LocalDateTime.now());

                long count = bookCopyRepository.countByBook_IdAndStatus(request.getBook().getId(),
                                BookCopyStatus.AVAILABLE);
                return mapper.toResponse(requestRepository.save(request), count);
        }

        @Override
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

                long count = bookCopyRepository.countByBook_IdAndStatus(request.getBook().getId(),
                                BookCopyStatus.AVAILABLE);
                return mapper.toResponse(requestRepository.save(request), count);
        }

        @Override
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

                long count = bookCopyRepository.countByBook_IdAndStatus(request.getBook().getId(),
                                BookCopyStatus.AVAILABLE);
                return mapper.toResponse(requestRepository.save(request), count);
        }
}
