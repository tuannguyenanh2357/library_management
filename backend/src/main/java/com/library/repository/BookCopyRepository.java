package com.library.repository;

import com.library.entity.BookCopy;
import com.library.entity.enums.BookCopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    // Tìm bản sao sách theo mã vạch.
    Optional<BookCopy> findByBarCode(String barCode);

    // Lấy danh sách bản sao theo sách và tải luôn thông tin Book
    @Query("SELECT bc FROM BookCopy bc JOIN FETCH bc.book WHERE bc.book.id = :bookId")
    List<BookCopy> findByBook_Id(Long bookId);

    // Lấy danh sách bản sao theo trạng thái và tải luôn thông tin Book.
    @Query("SELECT bc FROM BookCopy bc JOIN FETCH bc.book WHERE bc.status = :status")
    List<BookCopy> findByStatus(BookCopyStatus status);

    // Đếm số lượng bản sao của một đầu sách theo trạng thái.
    @Query("SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book.id = :bookId AND bc.status = :status")
    long countByBook_IdAndStatus(long bookId, BookCopyStatus status);

    // Khóa bản ghi ở mức ghi để đảm bảo chỉ một transaction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BookCopy> findFirstByBook_IdAndStatus(long bookId, BookCopyStatus status);

    // Đếm tổng số bản sao của một đầu sách.
    @Query("SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book.id = :bookId")
    long countByBook_Id(long bookId);

    // Lấy toàn bộ bản sao và tải luôn thông tin Book để tối ưu khi hiển thị danh
    // sách.
    @Query("SELECT bc FROM BookCopy bc JOIN FETCH bc.book")
    List<BookCopy> findAllWithBook();
}
