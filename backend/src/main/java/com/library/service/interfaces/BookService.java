package com.library.service.interfaces;

import com.library.dto.request.UpdateBookRequest;
import com.library.dto.request.CreateBookRequest;
import com.library.dto.response.BookResponse;
import java.util.List;

public interface BookService {
    BookResponse createBook(CreateBookRequest createBookRequest);
    BookResponse updateBook(Long bookId, UpdateBookRequest request);
    BookResponse getBookById(Long bookId);
    List<BookResponse> getAllBooks();
    List<BookResponse> getPopularBooks();
    void deleteBook(Long bookId);
}
