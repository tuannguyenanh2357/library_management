package com.library.service.impl;

import com.library.dto.response.BorrowingResponse;
import com.library.dto.response.OverdueBookProjection;
import com.library.dto.request.BorrowingCreationRequest;
import com.library.entity.BookCopy;
import com.library.entity.Borrowing;
import com.library.entity.Fines;
import com.library.entity.Member;
import com.library.entity.enums.BookCopyStatus;
import com.library.entity.enums.BorrowingStatus;
import com.library.entity.enums.ReservationStatus;
import com.library.entity.enums.FineStatus;
import com.library.mapper.BorrowingMapper;
import com.library.repository.BookCopyRepository;
import com.library.repository.BorrowingRepository;
import com.library.repository.FineRepository;
import com.library.repository.MemberRepository;
import com.library.service.interfaces.BorrowingService;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.exception.MemberNotFoundException;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import com.library.config.RabbitMQConfig;
import com.library.dto.event.BorrowingCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.library.service.interfaces.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BorrowingServiceImpl implements BorrowingService {

    BorrowingRepository borrowingRepository;
    MemberRepository memberRepository;
    BookCopyRepository bookCopyRepository;
    FineRepository fineRepository;
    ReservationService reservationService;
    BorrowingMapper mapper;
    RabbitTemplate rabbitTemplate;
    static final int MAX_RENEWALS = 1;
    static final int RENEWAL_EXTENSION_DAYS = 7;

    @org.springframework.beans.factory.annotation.Value("${app.library.fine.default-daily-amount:5000}")
    private BigDecimal defaultDailyFineAmount;

    @org.springframework.beans.factory.annotation.Value("${app.library.fine.default-replacement-fee:200000}")
    private BigDecimal defaultReplacementFee;

    @Override
    public BorrowingResponse borrowBook(BorrowingCreationRequest request) {

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));

        if (member.hasOverdueBorrowings()) {
            throw new AppException(ErrorCode.HAS_OVERDUE_BOOKS);
        }
        if (fineRepository.existsByBorrowingMemberIdAndStatus(member.getId(), FineStatus.UNPAID)) {
            throw new AppException(ErrorCode.HAS_UNPAID_FINES);
        }

        BookCopy bookCopy = bookCopyRepository.findById(request.getBookCopyId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_COPY_NOT_FOUND,
                        "Không tìm thấy bản sao sách"));

        if (bookCopy.getStatus() != BookCopyStatus.AVAILABLE) {
            if (bookCopy.getStatus() == BookCopyStatus.RESERVED) {
                // Kiểm tra xem người đang mượn có phải là người đã đặt trước cuốn này không
                boolean isReservedForThisMember = reservationService.getMyReservations(member.getUsername()).stream()
                        .anyMatch(res -> res.getStatus() == ReservationStatus.FULFILLED
                                && res.getFulfilledCopyId().equals(bookCopy.getId()));

                if (!isReservedForThisMember) {
                    throw new AppException(ErrorCode.BOOK_NOT_AVAILABLE,
                            "Bản sao này đã được giữ chỗ cho một độc giả khác.");
                }

                // Nếu đúng người, cần hoàn tất reservation
                reservationService.completeReservationByCopyId(bookCopy.getId());
            } else {
                throw new AppException(ErrorCode.BOOK_NOT_AVAILABLE);
            }
        }

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() : borrowDate.plusDays(14);

        if (dueDate.isBefore(borrowDate)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Ngày hẹn trả không thể ở trong quá khứ");
        }

        if (ChronoUnit.DAYS.between(borrowDate, dueDate) > 30) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Thời hạn mượn sách không được vượt quá 30 ngày");
        }

        Borrowing borrowing = Borrowing.builder()
                .member(member)
                .bookCopy(bookCopy)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .status(BorrowingStatus.ACTIVE)
                .build();

        bookCopy.markAsBorrowed();

        borrowingRepository.save(borrowing);
        bookCopyRepository.save(bookCopy);

        // Bắn sự kiện RabbitMQ bất đồng bộ để gửi Email thông báo mượn sách thành công
        BorrowingCreatedEvent event = BorrowingCreatedEvent.builder()
                .borrowingId(borrowing.getId())
                .memberEmail(member.getEmail())
                .memberName(member.getName())
                .bookTitle(bookCopy.getBook().getTitle())
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.RESERVATION_EXCHANGE,
                    RabbitMQConfig.BORROWING_CREATED_ROUTING_KEY, event);
            log.info("[RABBITMQ PRODUCER] Đã gửi BorrowingCreatedEvent cho phiếu mượn ID: {}", borrowing.getId());
        } catch (Exception e) {
            log.error("Lỗi khi gửi RabbitMQ event cho phiếu mượn ID {}: {}", borrowing.getId(), e.getMessage());
        }

        return mapper.toResponse(borrowing);
    }

    @Override
    public BorrowingResponse returnBook(Long borrowingId) {

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BORROWING_NOT_FOUND));

        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new AppException(ErrorCode.BORROWING_ACTION_FAILED, "Book already returned");
        }

        borrowing.setReturnDate(LocalDate.now());
        borrowing.setStatus(BorrowingStatus.RETURNED);

        BookCopy bookCopy = borrowing.getBookCopy();

        if (borrowing.getReturnDate().isAfter(borrowing.getDueDate())) {
            // tính số ngày quá hạn
            long overdueDays = ChronoUnit.DAYS.between(
                    borrowing.getDueDate(),
                    borrowing.getReturnDate());

            // lấy số tiền phạt 1 ngày của cuốn sách (nếu không có thì mặc định)
            BigDecimal dailyFine = bookCopy.getBook().getDailyFineAmount() != null
                    ? bookCopy.getBook().getDailyFineAmount()
                    : defaultDailyFineAmount;
            // nhân số ngày quá hạn với số tiền phạt 1 ngày
            BigDecimal amount = dailyFine.multiply(BigDecimal.valueOf(overdueDays));

            // tạo record phạt (Fines) với trạng thái UNPAID
            Fines fine = Fines.builder()
                    .borrowing(borrowing)
                    .amount(amount)
                    .reason("Trả sách quá hạn")
                    .issuedDate(LocalDate.now())
                    .status(FineStatus.UNPAID)
                    .build();

            fineRepository.save(fine);
        }

        borrowingRepository.save(borrowing);

        // Nếu có người đang xếp hàng chờ, sách sẽ đổi thành RESERVED. Ngược lại nó sẽ
        // thành AVAILABLE.
        reservationService.fulfillNextReservationIfAny(bookCopy.getBook().getId(), bookCopy);

        return mapper.toResponse(borrowing);
    }

    @Override
    public BorrowingResponse getById(Long id) {
        return mapper.toResponse(borrowingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrowing not found")));
    }

    @Override
    public List<BorrowingResponse> getAll() {

        return borrowingRepository.findAllWithRelations()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<BorrowingResponse> getByMemberId(Long memberId) {

        return borrowingRepository.findByMemberIdWithRelations(memberId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<BorrowingResponse> getOverdueBorrowings() {

        return borrowingRepository.findOverdueBorrowings(LocalDate.now())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<BorrowingResponse> getByCopyId(Long copyId) {
        return borrowingRepository.findByBookCopyIdWithRelations(copyId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<OverdueBookProjection> getOverdueBooksFromSP() {
        return borrowingRepository.getOverdueBooksFromSP();
    }

    @Override
    public void deleteBorrowing(Long borrowingId) {

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BORROWING_NOT_FOUND));

        if (borrowing.getFine() != null && borrowing.getFine().getStatus() == FineStatus.UNPAID) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Không thể xóa phiếu mượn vì độc giả chưa thanh toán tiền phạt đính kèm.");
        }

        borrowingRepository.delete(borrowing);
    }

    @Override
    public BorrowingResponse renewBorrowing(Long borrowingId, String username) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BORROWING_NOT_FOUND));

        if (!borrowing.getMember().getUsername().equals(username)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền gia hạn phiếu mượn này");
        }
        if (borrowing.getStatus() != BorrowingStatus.ACTIVE) {
            throw new AppException(ErrorCode.CANNOT_EXTEND_BORROWING, "Phiếu mượn không còn hoạt động");
        }
        if (LocalDate.now().isAfter(borrowing.getDueDate())) {
            throw new AppException(ErrorCode.CANNOT_EXTEND_BORROWING, "Không thể gia hạn sách đã quá hạn");
        }
        if (borrowing.getRenewalCount() >= MAX_RENEWALS) {
            throw new AppException(ErrorCode.CANNOT_EXTEND_BORROWING, "Bạn chỉ được gia hạn một lần cho mỗi lượt mượn");
        }

        Long bookId = borrowing.getBookCopy().getBook().getId();
        if (reservationService.hasPendingReservations(bookId)) {
            throw new AppException(ErrorCode.CANNOT_EXTEND_BORROWING,
                    "Không thể gia hạn vì đang có độc giả khác chờ mượn cuốn sách này");
        }

        borrowing.setDueDate(borrowing.getDueDate().plusDays(RENEWAL_EXTENSION_DAYS));
        borrowing.setRenewalCount(borrowing.getRenewalCount() + 1);
        borrowingRepository.save(borrowing);

        return mapper.toResponse(borrowing);
    }

    @Override
    public BorrowingResponse reportLost(Long borrowingId) {
        return closeWithReplacementFee(borrowingId, true);
    }

    @Override
    public BorrowingResponse reportDamaged(Long borrowingId) {
        return closeWithReplacementFee(borrowingId, false);
    }

    private BorrowingResponse closeWithReplacementFee(Long borrowingId, boolean lost) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BORROWING_NOT_FOUND));

        if (borrowing.getStatus() != BorrowingStatus.ACTIVE) {
            throw new AppException(ErrorCode.BORROWING_ACTION_FAILED,
                    "Chỉ có thể báo mất/hỏng với phiếu mượn đang hoạt động");
        }

        BookCopy bookCopy = borrowing.getBookCopy();
        if (lost) {
            bookCopy.markAsLost();
        } else {
            bookCopy.markAsDamaged();
        }
        bookCopyRepository.save(bookCopy);

        borrowing.setReturnDate(LocalDate.now());
        borrowing.setStatus(BorrowingStatus.RETURNED);
        borrowingRepository.save(borrowing);

        BigDecimal fee = bookCopy.getBook().getReplacementFee() != null
                ? bookCopy.getBook().getReplacementFee()
                : defaultReplacementFee;

        Fines fine = Fines.builder()
                .borrowing(borrowing)
                .amount(fee)
                .reason(lost ? "Đền bù sách bị mất" : "Đền bù sách bị hỏng")
                .issuedDate(LocalDate.now())
                .status(FineStatus.UNPAID)
                .build();
        fineRepository.save(fine);

        return mapper.toResponse(borrowing);
    }
}