package com.library.service.interfaces;

import com.library.dto.request.UpdateBookRequest;
import com.library.dto.request.CreateBookRequest;
import com.library.dto.response.BookResponse;
import com.library.dto.response.PageResponse;
import com.library.dto.response.TopBookResponse;
import java.util.List;

public interface BookService {
    BookResponse createBook(CreateBookRequest createBookRequest);

    BookResponse updateBook(Long bookId, UpdateBookRequest request);

    BookResponse getBookById(Long bookId);

    PageResponse<BookResponse> getAllBooks(Long id, String title, String author, String category, String publisher,
            String isbn, int page, int size);

    List<TopBookResponse> getTop10MostBorrowedBooks();

    void deleteBook(Long bookId);

    List<String> getUniqueCategories();
}
