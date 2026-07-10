package com.library.repository;

import com.library.entity.Borrowing;
import com.library.entity.enums.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    List<Borrowing> findByMemberId(Long memberId);
    List<Borrowing> findByStatus(BorrowingStatus status);
}
