package com.library.repository;

import com.library.entity.BookCopy;
import com.library.entity.enums.BookCopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    Optional<BookCopy> findByBarCode(String barCode);

    @Query("SELECT bc FROM BookCopy bc JOIN FETCH bc.book WHERE bc.book.id = :bookId")
    List<BookCopy> findByBook_Id(Long bookId);

    @Query("SELECT bc FROM BookCopy bc JOIN FETCH bc.book WHERE bc.status = :status")
    List<BookCopy> findByStatus(BookCopyStatus status);

    @Query("SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book.id = :bookId AND bc.status = :status")
    long countByBook_IdAndStatus(long bookId, BookCopyStatus status);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<BookCopy> findFirstByBook_IdAndStatus(long bookId, BookCopyStatus status);

    @Query("SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book.id = :bookId")
    long countByBook_Id(long bookId);

    @Query("SELECT bc FROM BookCopy bc JOIN FETCH bc.book")
    List<BookCopy> findAllWithBook();
}
