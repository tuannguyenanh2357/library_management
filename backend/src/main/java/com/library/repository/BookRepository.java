package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);
    Optional<Book> findByIsbn(String isbn);
    Optional<Book> findByTitle(String title);
    List<Book> findByAuthor(String author);
    List<Book> findByCategory(String category);
    List<Book> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT b FROM Book b LEFT JOIN b.copies c LEFT JOIN c.borrowings br ON br.borrowDate >= :startDate " +
           "GROUP BY b.id, b.title, b.author, b.publisher, b.isbn, b.category, b.description, b.imageUrl, b.publicationYear, b.createdAt, b.updatedAt " +
           "ORDER BY COUNT(br) DESC")
    List<Book> findTop10MostBorrowedSince(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate, Pageable pageable);
}

