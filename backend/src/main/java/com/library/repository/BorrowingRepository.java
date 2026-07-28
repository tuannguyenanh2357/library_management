package com.library.repository;

import com.library.entity.Borrowing;
import com.library.entity.enums.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

import com.library.dto.response.OverdueBookProjection;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    @Query(value = "EXEC dbo.GetOverdueBooks", nativeQuery = true)
    List<OverdueBookProjection> getOverdueBooksFromSP();

    @Query("SELECT b FROM Borrowing b " + "JOIN FETCH b.member m " + "JOIN FETCH b.bookCopy bc " + "JOIN FETCH bc.book bk")
    List<Borrowing> findAllWithRelations();

    @Query("SELECT b FROM Borrowing b " + "JOIN FETCH b.member m " + "JOIN FETCH b.bookCopy bc " + "JOIN FETCH bc.book bk " + "WHERE b.status = 'ACTIVE' AND b.dueDate < :today")
    List<Borrowing> findOverdueBorrowings(@Param("today") LocalDate today);

    @Query("SELECT b FROM Borrowing b WHERE b.member.id = :memberId")
    List<Borrowing> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT b FROM Borrowing b WHERE b.status = :status")
    List<Borrowing> findByStatus(@Param("status") BorrowingStatus status);

    long countByStatus(BorrowingStatus status);

    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.status = 'ACTIVE' AND b.dueDate < :today")
    long countOverdueBorrowings(@Param("today") LocalDate today);

    @Query("SELECT b FROM Borrowing b JOIN FETCH b.member m JOIN FETCH b.bookCopy bc JOIN FETCH bc.book bk WHERE b.member.id = :memberId")
    List<Borrowing> findByMemberIdWithRelations(@Param("memberId") Long memberId);

    @Query("SELECT b FROM Borrowing b " + "JOIN FETCH b.member m " + "JOIN FETCH b.bookCopy bc " +  "JOIN FETCH bc.book bk " +  "WHERE b.status = 'ACTIVE' AND b.dueDate < :today")
    List<Borrowing> findOverdueBorrowingsWithRelations(@Param("today") LocalDate today);

    @Query("SELECT b FROM Borrowing b " + "JOIN FETCH b.member m " + "JOIN FETCH b.bookCopy bc " + "JOIN FETCH bc.book bk " + "WHERE bc.id = :copyId ORDER BY b.borrowDate DESC")
    List<Borrowing> findByBookCopyIdWithRelations(@Param("copyId") Long copyId);

    @Query("SELECT MIN(b.dueDate) FROM Borrowing b WHERE b.bookCopy.book.id = :bookId AND b.status = 'ACTIVE'")
    LocalDate findEarliestDueDateByBookId(@Param("bookId") Long bookId);

}
