package com.library.repository;

import com.library.dto.response.TopBookProjection;
import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

        // Kiểm tra ISBN đã tồn tại hay chưa.
        boolean existsByIsbn(String isbn);

        // Tìm sách theo ISBN.
        Optional<Book> findByIsbn(String isbn);

        // Tìm sách theo tiêu đề.
        Optional<Book> findByTitle(String title);

        // Lấy danh sách sách của một tác giả.
        List<Book> findByAuthor(String author);

        // Lấy danh sách sách theo thể loại.
        List<Book> findByCategory(String category);

        // Tìm kiếm sách theo tiêu đề (không phân biệt hoa/thường).
        List<Book> findByTitleContainingIgnoreCase(String title);

        // Gọi Stored Procedure để lấy Top 10 sách được mượn nhiều nhất.
        @Query(value = "EXEC dbo.GetTop10MostBorrowedBooks", nativeQuery = true)
        List<TopBookProjection> getTop10MostBorrowedBooks();

        // Tìm kiếm sách theo các tiêu chí lọc tùy chọn và hỗ trợ phân trang.
        // Điều kiện có giá trị sẽ được áp dụng, điều kiện null sẽ được bỏ qua.
        @Query("SELECT b FROM Book b WHERE " +
                        "(:id IS NULL OR b.id = :id) AND " +
                        "(:title IS NULL OR LOWER(b.title) LIKE :title) AND " +
                        "(:author IS NULL OR LOWER(b.author) LIKE :author) AND " +
                        "(:category IS NULL OR LOWER(b.category) LIKE :category) AND " +
                        "(:publisher IS NULL OR LOWER(b.publisher) LIKE :publisher) AND " +
                        "(:isbn IS NULL OR LOWER(b.isbn) LIKE :isbn)")
        Page<Book> findByFilters(
                        @Param("id") Long id,
                        @Param("title") String title,
                        @Param("author") String author,
                        @Param("category") String category,
                        @Param("publisher") String publisher,
                        @Param("isbn") String isbn,
                        Pageable pageable);

        // Lấy danh sách thể loại duy nhất để phục vụ bộ lọc hoặc danh mục.
        @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL AND b.category <> ''")
        List<String> findUniqueCategories();
}