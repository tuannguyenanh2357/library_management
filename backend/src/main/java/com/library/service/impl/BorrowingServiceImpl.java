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
import com.library.entity.enums.DamageType;
import com.library.entity.enums.ReservationStatus;
import com.library.entity.enums.FineStatus;
import com.library.mapper.BorrowingMapper;
import com.library.repository.BookCopyRepository;
import com.library.repository.BorrowingRepository;
import com.library.repository.FineRepository;
import com.library.repository.MemberRepository;
import com.library.repository.ReservationRepository;
import com.library.service.interfaces.BorrowingService;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.exception.MemberNotFoundException;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import com.library.config.RabbitMQConfig;
import com.library.dto.event.BorrowingCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.library.service.interfaces.ReservationService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BorrowingServiceImpl implements BorrowingService {

    BorrowingRepository borrowingRepository;
    MemberRepository memberRepository;
    BookCopyRepository bookCopyRepository;
    FineRepository fineRepository;
    ReservationRepository reservationRepository;
    ReservationService reservationService;
    BorrowingMapper borrowingMapper;
    RabbitTemplate rabbitTemplate;

    @NonFinal
    @Value("${app.library.fine.default-daily-amount:5000}")
    BigDecimal defaultDailyFineAmount;

    @NonFinal
    @Value("${app.library.fine.default-replacement-fee:200000}")
    BigDecimal defaultReplacementFee;

    @NonFinal
    @Value("${app.library.borrowing.max-renewals:1}")
    int maxRenewals;

    @NonFinal
    @Value("${app.library.borrowing.renewal-extension-days:7}")
    int renewalExtensionDays;

    @NonFinal
    @Value("${app.library.borrowing.default-days:14}")
    long defaultBorrowingDays;

    @NonFinal
    @Value("${app.library.borrowing.max-days:30}")
    long maxBorrowingDays;

    @Override
    @CacheEvict(value = { "books", "topBooks" }, allEntries = true)
    public BorrowingResponse borrowBook(BorrowingCreationRequest request) {

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));

        validateMemberEligibleToBorrow(member);

        BookCopy bookCopy = bookCopyRepository.findById(request.getBookCopyId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_COPY_NOT_FOUND,
                        "Không tìm thấy bản sao sách"));

        resolveBookCopyForBorrowing(bookCopy, member);

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = request.getDueDate() != null ? request.getDueDate()
                : borrowDate.plusDays(defaultBorrowingDays);
        validateDueDate(borrowDate, dueDate);

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

        publishBorrowingCreatedEvent(borrowing, member, bookCopy, borrowDate, dueDate);

        return borrowingMapper.toResponse(borrowing);
    }

    private void validateMemberEligibleToBorrow(Member member) {
        if (member.hasOverdueBorrowings()) {
            throw new AppException(ErrorCode.HAS_OVERDUE_BOOKS);
        }
        if (fineRepository.existsByBorrowingMemberIdAndStatus(member.getId(), FineStatus.UNPAID)) {
            throw new AppException(ErrorCode.HAS_UNPAID_FINES);
        }
    }

    // Nếu bản sao không AVAILABLE chỉ hợp lệ khi đang RESERVED cho đúng member này
    // trong trường hợp đó cần hoàn tất reservation tương ứng.
    private void resolveBookCopyForBorrowing(BookCopy bookCopy, Member member) {
        if (bookCopy.getStatus() == BookCopyStatus.AVAILABLE) {
            return;
        }

        if (bookCopy.getStatus() != BookCopyStatus.RESERVED) {
            throw new AppException(ErrorCode.BOOK_NOT_AVAILABLE);
        }

        boolean isReservedForThisMember = reservationRepository.existsByMemberIdAndFulfilledCopyIdAndStatus(
                member.getId(), bookCopy.getId(), ReservationStatus.FULFILLED);

        if (!isReservedForThisMember) {
            throw new AppException(ErrorCode.BOOK_NOT_AVAILABLE,
                    "Bản sao này đã được giữ chỗ cho một độc giả khác.");
        }

        reservationService.completeReservationByCopyId(bookCopy.getId());
    }

    private void validateDueDate(LocalDate borrowDate, LocalDate dueDate) {
        if (dueDate.isBefore(borrowDate)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Ngày hẹn trả không thể ở trong quá khứ");
        }
        if (ChronoUnit.DAYS.between(borrowDate, dueDate) > maxBorrowingDays) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Thời hạn mượn sách không được vượt quá " + maxBorrowingDays + " ngày");
        }
    }

    // Bắn sự kiện RabbitMQ bất đồng bộ để gửi Email thông báo mượn sách thành công
    private void publishBorrowingCreatedEvent(Borrowing borrowing, Member member, BookCopy bookCopy,
            LocalDate borrowDate, LocalDate dueDate) {
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
    }

    @Override
    @CacheEvict(value = { "books", "topBooks" }, allEntries = true)
    public BorrowingResponse returnBook(Long borrowingId) {

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BORROWING_NOT_FOUND));

        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new AppException(ErrorCode.BORROWING_ACTION_FAILED, "Sách đã được trả lại rồi");
        }

        borrowing.setReturnDate(LocalDate.now());
        borrowing.setStatus(BorrowingStatus.RETURNED);

        BookCopy bookCopy = borrowing.getBookCopy();

        if (borrowing.getReturnDate().isAfter(borrowing.getDueDate())) {
            calculateAndSaveOverdueFine(borrowing, bookCopy);
        }

        borrowingRepository.save(borrowing);

        // Nếu có người đang xếp hàng chờ, sách sẽ đổi thành RESERVED. Ngược lại nó sẽ
        // thành AVAILABLE.
        reservationService.fulfillNextReservationIfAny(bookCopy.getBook().getId(), bookCopy);

        return borrowingMapper.toResponse(borrowing);
    }

    private void calculateAndSaveOverdueFine(Borrowing borrowing, BookCopy bookCopy) {
        long overdueDays = ChronoUnit.DAYS.between(borrowing.getDueDate(), borrowing.getReturnDate());

        BigDecimal dailyFine = bookCopy.getBook().getDailyFineAmount() != null
                ? bookCopy.getBook().getDailyFineAmount()
                : defaultDailyFineAmount;

        BigDecimal amount = dailyFine.multiply(BigDecimal.valueOf(overdueDays));

        fineRepository.save(createFine(borrowing, amount, "Trả sách quá hạn"));
    }

    private Fines createFine(Borrowing borrowing, BigDecimal amount, String reason) {
        return Fines.builder()
                .borrowing(borrowing)
                .amount(amount)
                .reason(reason)
                .issuedDate(LocalDate.now())
                .status(FineStatus.UNPAID)
                .build();
    }

    @Override
    public BorrowingResponse getById(Long id) {
        return borrowingMapper.toResponse(borrowingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BORROWING_NOT_FOUND)));
    }

    @Override
    public List<BorrowingResponse> getAll() {

        return borrowingRepository.findAllWithRelations()
                .stream()
                .map(borrowingMapper::toResponse)
                .toList();
    }

    @Override
    public List<BorrowingResponse> getByMemberId(Long memberId) {

        return borrowingRepository.findByMemberIdWithRelations(memberId)
                .stream()
                .map(borrowingMapper::toResponse)
                .toList();
    }

    @Override
    public List<BorrowingResponse> getOverdueBorrowings() {

        return borrowingRepository.findOverdueBorrowings(LocalDate.now())
                .stream()
                .map(borrowingMapper::toResponse)
                .toList();
    }

    @Override
    public List<BorrowingResponse> getByCopyId(Long copyId) {
        return borrowingRepository.findByBookCopyIdWithRelations(copyId)
                .stream()
                .map(borrowingMapper::toResponse)
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
        if (borrowing.getRenewalCount() >= maxRenewals) {
            throw new AppException(ErrorCode.CANNOT_EXTEND_BORROWING, "Bạn chỉ được gia hạn một lần cho mỗi lượt mượn");
        }

        Long bookId = borrowing.getBookCopy().getBook().getId();
        if (reservationService.hasPendingReservations(bookId)) {
            throw new AppException(ErrorCode.CANNOT_EXTEND_BORROWING,
                    "Không thể gia hạn vì đang có độc giả khác chờ mượn cuốn sách này");
        }

        borrowing.setDueDate(borrowing.getDueDate().plusDays(renewalExtensionDays));
        borrowing.setRenewalCount(borrowing.getRenewalCount() + 1);
        borrowingRepository.save(borrowing);

        return borrowingMapper.toResponse(borrowing);
    }

    @Override
    @CacheEvict(value = { "books", "topBooks" }, allEntries = true)
    public BorrowingResponse reportLost(Long borrowingId) {
        return closeWithReplacementFee(borrowingId, DamageType.LOST);
    }

    @Override
    @CacheEvict(value = { "books", "topBooks" }, allEntries = true)
    public BorrowingResponse reportDamaged(Long borrowingId) {
        return closeWithReplacementFee(borrowingId, DamageType.DAMAGED);
    }

    private BorrowingResponse closeWithReplacementFee(Long borrowingId, DamageType damageType) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BORROWING_NOT_FOUND));

        if (borrowing.getStatus() != BorrowingStatus.ACTIVE) {
            throw new AppException(ErrorCode.BORROWING_ACTION_FAILED,
                    "Chỉ có thể báo mất/hỏng với phiếu mượn đang hoạt động");
        }

        BookCopy bookCopy = borrowing.getBookCopy();
        if (damageType == DamageType.LOST) {
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

        String reason = damageType == DamageType.LOST ? "Đền bù sách bị mất" : "Đền bù sách bị hỏng";
        fineRepository.save(createFine(borrowing, fee, reason));

        return borrowingMapper.toResponse(borrowing);
    }
}
