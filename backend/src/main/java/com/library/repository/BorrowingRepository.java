package com.library.repository;

import com.library.entity.Borrowing;
import com.library.entity.enums.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.library.dto.response.OverdueBookProjection;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

        // Lấy toàn bộ phiếu mượn và tải sẵn thông tin Member, BookCopy, Book
        @Query("SELECT b FROM Borrowing b " + "JOIN FETCH b.member m " + "JOIN FETCH b.bookCopy bc "
                        + "JOIN FETCH bc.book bk")
        List<Borrowing> findAllWithRelations();

        Optional<Borrowing> findByBookCopyIdAndStatus(Long bookCopyId, BorrowingStatus status);

        // Lấy danh sách phiếu mượn đang hoạt động nhưng đã quá hạn trả.
        @Query("SELECT b FROM Borrowing b " +
                        "JOIN FETCH b.member m " +
                        "JOIN FETCH b.bookCopy bc " +
                        "JOIN FETCH bc.book bk " +
                        "WHERE b.status = 'ACTIVE' AND b.dueDate < :today")
        List<Borrowing> findOverdueBorrowings(@Param("today") LocalDate today);

        // Lấy danh sách phiếu mượn của một thành viên.
        @Query("SELECT b FROM Borrowing b WHERE b.member.id = :memberId")
        List<Borrowing> findByMemberId(@Param("memberId") Long memberId);

        // Lấy danh sách phiếu mượn theo trạng thái.
        @Query("SELECT b FROM Borrowing b WHERE b.status = :status")
        List<Borrowing> findByStatus(@Param("status") BorrowingStatus status);

        // Đếm số lượng phiếu mượn theo trạng thái.
        long countByStatus(BorrowingStatus status);

        // Đếm số lượng phiếu mượn đang quá hạn.
        @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.status = 'ACTIVE' AND b.dueDate < :today")
        long countOverdueBorrowings(@Param("today") LocalDate today);

        // Lấy phiếu mượn của một thành viên và tải sẵn các thông tin liên quan.
        @Query("SELECT b FROM Borrowing b " +
                        "JOIN FETCH b.member m " +
                        "JOIN FETCH b.bookCopy bc " +
                        "JOIN FETCH bc.book bk " +
                        "WHERE b.member.id = :memberId")
        List<Borrowing> findByMemberIdWithRelations(@Param("memberId") Long memberId);

        // Lấy danh sách phiếu mượn quá hạn và tải sẵn các thông tin liên quan.
        @Query("SELECT b FROM Borrowing b " +
                        "JOIN FETCH b.member m " +
                        "JOIN FETCH b.bookCopy bc " +
                        "JOIN FETCH bc.book bk " +
                        "WHERE b.status = 'ACTIVE' AND b.dueDate < :today")
        List<Borrowing> findOverdueBorrowingsWithRelations(@Param("today") LocalDate today);

        // Lấy lịch sử mượn của một bản sao sách, sắp xếp theo ngày mượn mới nhất.
        @Query("SELECT b FROM Borrowing b " +
                        "JOIN FETCH b.member m " +
                        "JOIN FETCH b.bookCopy bc " +
                        "JOIN FETCH bc.book bk " +
                        "WHERE bc.id = :copyId ORDER BY b.borrowDate DESC")
        List<Borrowing> findByBookCopyIdWithRelations(@Param("copyId") Long copyId);

        // Lấy ngày phải trả sớm nhất của các bản sao đang được mượn thuộc một đầu sách.
        @Query("SELECT MIN(b.dueDate) FROM Borrowing b WHERE b.bookCopy.book.id = :bookId AND b.status = 'ACTIVE'")
        LocalDate findEarliestDueDateByBookId(@Param("bookId") Long bookId);

        // Lấy ngày phải trả sớm nhất theo từng đầu sách trong một danh sách, dùng để
        // tránh N+1 query khi cần tính ngày dự kiến có sách cho nhiều đầu sách cùng
        // lúc.
        @Query("SELECT b.bookCopy.book.id, MIN(b.dueDate) FROM Borrowing b " +
                        "WHERE b.bookCopy.book.id IN :bookIds AND b.status = 'ACTIVE' " +
                        "GROUP BY b.bookCopy.book.id")
        List<Object[]> findEarliestDueDatesByBookIds(@Param("bookIds") List<Long> bookIds);

        // Lấy danh sách các lượt mượn quá hạn
        @Query("SELECT br.id AS borrowingId, m.id AS memberId, m.name AS memberName, m.phone AS memberPhone, " +
                        "bk.title AS bookTitle, bc.barCode AS barCode, br.borrowDate AS borrowDate, br.dueDate AS dueDate "
                        +
                        "FROM Borrowing br " +
                        "JOIN br.member m " +
                        "JOIN br.bookCopy bc " +
                        "JOIN bc.book bk " +
                        "WHERE br.status = 'ACTIVE' AND br.dueDate < CURRENT_DATE " +
                        "ORDER BY br.dueDate ASC")
        List<OverdueBookProjection> getOverdueBooksFromSP();

}