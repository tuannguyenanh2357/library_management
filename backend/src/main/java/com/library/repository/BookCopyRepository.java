package com.library.repository;

import com.library.entity.BookCopy;
import com.library.entity.enums.BookCopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    Optional<BookCopy> findByBarCode(String barCode);
    List<BookCopy> findByBook_Id(Long bookId);
    List<BookCopy> findByStatus(BookCopyStatus status);
    long countByBook_Id(long bookId);
}
