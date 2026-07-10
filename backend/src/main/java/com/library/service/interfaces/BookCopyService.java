package com.library.service.interfaces;

import com.library.dto.request.BookCopyCreationRequest;
import com.library.dto.request.BookCopyUpdateRequest;
import com.library.dto.response.BookCopyResponse;

import java.util.List;

public interface BookCopyService {
    BookCopyResponse createBookCopy(BookCopyCreationRequest request);
    BookCopyResponse updateBookCopy(Long bookCopyId, BookCopyUpdateRequest request);
    BookCopyResponse getBookCopyById(Long bookCopyId);
    List<BookCopyResponse> getAllBookCopies();
    void deleteBookCopy(Long bookCopyId);
}
