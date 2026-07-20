package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
           "GROUP BY b.id, b.title, b.author, b.publisher, b.isbn, b.category, b.description, b.imageUrl, b.publicationYear, b.dailyFineAmount, b.createdAt, b.updatedAt " +
           "ORDER BY COUNT(br) DESC")
    List<Book> findTop10MostBorrowedSince(@Param("startDate") java.time.LocalDate startDate, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE " +
           "(:id IS NULL OR b.id = :id) AND " +
           "(:title IS NULL OR :title = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR :author = '' OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
           "(:publisher IS NULL OR :publisher = '' OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :publisher, '%'))) AND " +
           "(:isbn IS NULL OR :isbn = '' OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :isbn, '%')))")
    Page<Book> findByFilters(
        @Param("id") Long id,
        @Param("title") String title,
        @Param("author") String author,
        @Param("category") String category,
        @Param("publisher") String publisher,
        @Param("isbn") String isbn,
        Pageable pageable
    );

    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL AND b.category <> ''")
    List<String> findUniqueCategories();
}

