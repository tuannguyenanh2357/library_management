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
    final BorrowingMapper mapper;

    @Override
    public BorrowingResponse borrowBook(BorrowingCreationRequest request) {

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        BookCopy bookCopy = bookCopyRepository.findById(request.getBookCopyId())
                .orElseThrow(() -> new RuntimeException("Book copy not found"));

        if (bookCopy.getStatus() != BookCopyStatus.AVAILABLE) {
            throw new RuntimeException("Book is not available");
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
        bookCopy.markAsReturned();

        if (borrowing.getReturnDate().isAfter(borrowing.getDueDate())) {

            long overdueDays = ChronoUnit.DAYS.between(
                    borrowing.getDueDate(),
                    borrowing.getReturnDate());

            BigDecimal amount = BigDecimal.valueOf(overdueDays * 5000);

            Fines fine = Fines.builder()
                    .borrowing(borrowing)
                    .amount(amount)
                    .reason("Returned book late")
                    .issuedDate(LocalDate.now())
                    .status(FineStatus.UNPAID)
                    .build();

            fineRepository.save(fine);
        }

        borrowingRepository.save(borrowing);
        bookCopyRepository.save(bookCopy);

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
    public void deleteBorrowing(Long borrowingId) {

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Borrowing not found"));

        borrowingRepository.delete(borrowing);
    }
}