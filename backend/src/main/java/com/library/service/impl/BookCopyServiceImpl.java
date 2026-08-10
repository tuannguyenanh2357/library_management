package com.library.service.impl;

import com.library.dto.request.BookCopyCreationRequest;
import com.library.dto.request.BookCopyUpdateRequest;
import com.library.dto.response.BookCopyResponse;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.mapper.BookCopyMapper;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.service.interfaces.BookCopyService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookCopyServiceImpl implements BookCopyService {
    BookCopyRepository bookCopyRepository;
    BookRepository bookRepository;
    BookCopyMapper bookCopyMapper;

    @Override
    public List<BookCopyResponse> getAllBookCopies() {
        return bookCopyRepository.findAll().stream()
                .map(bookCopyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookCopyResponse getBookCopyById(Long bookCopyId) {
        BookCopy bookCopy = bookCopyRepository.findById(bookCopyId)
                .orElseThrow(() -> new RuntimeException("Book copy not found"));
        return bookCopyMapper.toResponse(bookCopy);
    }

    @Override
    public BookCopyResponse createBookCopy(BookCopyCreationRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        BookCopy bookCopy = bookCopyMapper.toBookCopy(request);
        bookCopy.setBook(book);
        if (bookCopy.getBarCode() == null || bookCopy.getBarCode().isBlank()) {
            bookCopy.setBarCode("BC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        }

        BookCopy savedBookCopy = bookCopyRepository.save(bookCopy);
        return bookCopyMapper.toResponse(savedBookCopy);
    }

    @Override
    public BookCopyResponse updateBookCopy(Long bookCopyId, BookCopyUpdateRequest request) {
        BookCopy existingBookCopy = bookCopyRepository.findById(bookCopyId)
                .orElseThrow(() -> new RuntimeException("Book copy not found"));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        bookCopyMapper.updateBookCopy(existingBookCopy, request);
        existingBookCopy.setBook(book);

        BookCopy updatedBookCopy = bookCopyRepository.save(existingBookCopy);
        return bookCopyMapper.toResponse(updatedBookCopy);
    }

    @Override
    public void deleteBookCopy(Long bookCopyId) {
        bookCopyRepository.deleteById(bookCopyId);
    }
}
