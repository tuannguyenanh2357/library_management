package com.library.service.impl;

import com.library.dto.response.DashboardStatsResponse;
import com.library.entity.enums.BorrowingStatus;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.BorrowingRepository;
import com.library.repository.MemberRepository;
import com.library.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final MemberRepository memberRepository;
    private final BorrowingRepository borrowingRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        long totalBooks = bookRepository.count();
        long totalBookCopies = bookCopyRepository.count();
        long totalMembers = memberRepository.count();
        long activeBorrowings = borrowingRepository.countByStatus(BorrowingStatus.ACTIVE);
        long overdueBorrowings = borrowingRepository.countOverdueBorrowings(LocalDate.now());

        return DashboardStatsResponse.builder()
                .totalBooks(totalBooks)
                .totalBookCopies(totalBookCopies)
                .totalMembers(totalMembers)
                .activeBorrowings(activeBorrowings)
                .overdueBorrowings(overdueBorrowings)
                .build();
    }
}
