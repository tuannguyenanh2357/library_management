package com.library.service.impl;

import com.library.dto.response.BorrowingResponse;
import com.library.dto.request.BorrowingCreationRequest;
import com.library.entity.BookCopy;
import com.library.entity.Borrowing;
import com.library.entity.Fines;
import com.library.entity.Member;
import com.library.entity.enums.BookCopyStatus;
import com.library.entity.enums.BorrowingStatus;
import com.library.entity.enums.FineStatus;
import com.library.mapper.BorrowingMapper;
import com.library.repository.BookCopyRepository;
import com.library.repository.BorrowingRepository;
import com.library.repository.FineRepository;
import com.library.repository.MemberRepository;
import com.library.service.interfaces.BorrowingService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Transactional
public class BorrowingServiceImpl implements BorrowingService {

    final BorrowingRepository borrowingRepository;
    final MemberRepository memberRepository;
    final BookCopyRepository bookCopyRepository;
    final FineRepository fineRepository;
    final com.library.service.interfaces.ReservationService reservationService;
    final BorrowingMapper mapper;

    @Override
    public BorrowingResponse borrowBook(BorrowingCreationRequest request) {

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả"));

        if (member.hasOverdueBorrowings()) {
            throw new RuntimeException("Không thể mượn sách mới khi đang có sách quá hạn chưa trả");
        }
        if (fineRepository.existsByBorrowing_Member_IdAndStatus(member.getId(), FineStatus.UNPAID)) {
            throw new RuntimeException("Không thể mượn sách mới khi đang có khoản phạt chưa thanh toán");
        }

        BookCopy bookCopy = bookCopyRepository.findById(request.getBookCopyId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản sao sách"));

        if (bookCopy.getStatus() != BookCopyStatus.AVAILABLE) {
            if (bookCopy.getStatus() == BookCopyStatus.RESERVED) {
                // Kiểm tra xem người đang mượn có phải là người đã đặt trước cuốn này không
                boolean isReservedForThisMember = reservationService.getMyReservations(member.getUsername()).stream()
                        .anyMatch(res -> res.getStatus() == com.library.entity.enums.ReservationStatus.FULFILLED && res.getFulfilledCopyId().equals(bookCopy.getId()));
                
                if (!isReservedForThisMember) {
                    throw new RuntimeException("Bản sao này đã được giữ chỗ cho một độc giả khác.");
                }
                
                // Nếu đúng người, cần hoàn tất reservation
                reservationService.completeReservationByCopyId(bookCopy.getId());
            } else {
                throw new RuntimeException("Book is not available");
            }
        }

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() : borrowDate.plusDays(14);

        if (dueDate.isBefore(borrowDate)) {
            throw new RuntimeException("Due date cannot be in the past");
        }

        if (ChronoUnit.DAYS.between(borrowDate, dueDate) > 30) {
            throw new RuntimeException("Borrowing duration cannot exceed 30 days");
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

        return mapper.toResponse(borrowing);
    }

    @Override
    public BorrowingResponse returnBook(Long borrowingId) {

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Borrowing not found"));

        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new RuntimeException("Book already returned");
        }

        borrowing.setReturnDate(LocalDate.now());
        borrowing.setStatus(BorrowingStatus.RETURNED);

        BookCopy bookCopy = borrowing.getBookCopy();
        // Xóa dòng markAsReturned() vì ReservationService sẽ lo việc này


        if (borrowing.getReturnDate().isAfter(borrowing.getDueDate())) {

            long overdueDays = ChronoUnit.DAYS.between(
                    borrowing.getDueDate(),
                    borrowing.getReturnDate());

            java.math.BigDecimal dailyFine = bookCopy.getBook().getDailyFineAmount() != null
                    ? bookCopy.getBook().getDailyFineAmount()
                    : java.math.BigDecimal.valueOf(5000);
            BigDecimal amount = dailyFine.multiply(BigDecimal.valueOf(overdueDays));

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
        
        // Thay vì tự động set thành AVAILABLE, chuyển quyền định đoạt cho ReservationService
        // Nếu có người đang xếp hàng chờ, sách sẽ đổi thành RESERVED. Ngược lại nó sẽ thành AVAILABLE.
        reservationService.fulfillNextReservationIfAny(bookCopy.getBook().getId(), bookCopy);

        return mapper.toResponse(borrowing);
    }

    @Override
    public BorrowingResponse getById(Long id) {

        return mapper.toResponse(
                borrowingRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Borrowing not found"))
        );
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
    public List<com.library.dto.response.OverdueBookProjection> getOverdueBooksFromSP() {
        return borrowingRepository.getOverdueBooksFromSP();
    }

    @Override
    public void deleteBorrowing(Long borrowingId) {

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Borrowing not found"));

        borrowingRepository.delete(borrowing);
    }

    private static final int MAX_RENEWALS = 1;
    private static final int RENEWAL_EXTENSION_DAYS = 7;

    @Override
    public BorrowingResponse renewBorrowing(Long borrowingId, String username) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Borrowing not found"));

        if (!borrowing.getMember().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền gia hạn phiếu mượn này");
        }
        if (borrowing.getStatus() != BorrowingStatus.ACTIVE) {
            throw new RuntimeException("Phiếu mượn không còn hoạt động");
        }
        if (LocalDate.now().isAfter(borrowing.getDueDate())) {
            throw new RuntimeException("Không thể gia hạn sách đã quá hạn");
        }
        if (borrowing.getRenewalCount() >= MAX_RENEWALS) {
            throw new RuntimeException("Bạn chỉ được gia hạn một lần cho mỗi lượt mượn");
        }

        Long bookId = borrowing.getBookCopy().getBook().getId();
        if (reservationService.hasPendingReservations(bookId)) {
            throw new RuntimeException("Không thể gia hạn vì đang có độc giả khác chờ mượn cuốn sách này");
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
                .orElseThrow(() -> new RuntimeException("Borrowing not found"));

        if (borrowing.getStatus() != BorrowingStatus.ACTIVE) {
            throw new RuntimeException("Chỉ có thể báo mất/hỏng với phiếu mượn đang hoạt động");
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
                : new BigDecimal("200000.00");

        Fines fine = Fines.builder()
                .borrowing(borrowing)
                .amount(fee)
                .reason(lost ? "Đền bù sách bị mất" : "Đền bù sách bị hỏng")
                .issuedDate(LocalDate.now())
                .status(FineStatus.UNPAID)
                .build();
        fineRepository.save(fine);

        // Không gọi fulfillNextReservationIfAny: bản sách mất/hỏng không thể giao cho ai,
        // reservation đang chờ tiếp tục đợi bản khác.

        return mapper.toResponse(borrowing);
    }
}