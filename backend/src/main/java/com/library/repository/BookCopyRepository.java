package com.library.repository;

import com.library.dto.response.BookCopyResponse;
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

    // Kiểm tra xem mã vạch đã tồn tại hay chưa.
    boolean existsByBarCode(String barCode);


    // Đếm số lượng bản sao của một đầu sách theo trạng thái.
    @Query("SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book.id = :bookId AND bc.status = :status")
    long countByBookIdAndStatus(long bookId, BookCopyStatus status);


    // Khóa bản ghi ở mức ghi để đảm bảo chỉ một transaction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BookCopy> findFirstByBook_IdAndStatus(long bookId, BookCopyStatus status);

    // Lấy tất cả bản sao dưới dạng Projection (DTO) kèm theo Hạn trả từ phiếu mượn ACTIVE
    @Query("SELECT new com.library.dto.response.BookCopyResponse(" +
           "bc.id, bc.barCode, bc.status, bc.book.author, bc.book.title, br.dueDate, bc.book.id) " +
           "FROM BookCopy bc " +
           "LEFT JOIN Borrowing br ON br.bookCopy = bc AND br.status = com.library.entity.enums.BorrowingStatus.ACTIVE " +
           "ORDER BY bc.id DESC")
    List<BookCopyResponse> findAllProjectedResponses();
}
