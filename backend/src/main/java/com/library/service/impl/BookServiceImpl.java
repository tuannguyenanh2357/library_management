package com.library.service.impl;

import com.library.dto.request.CreateBookRequest;
import com.library.dto.request.UpdateBookRequest;
import com.library.dto.response.BookResponse;
import com.library.entity.Book;
import com.library.repository.BookRepository;
import com.library.mapper.BookMapper;
import com.library.service.interfaces.BookService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
public class BookServiceImpl implements BookService {
    final BookRepository bookRepository;
    final BookMapper bookMapper;

    @Override
    public List<BookResponse> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> getPopularBooks() {
        java.time.LocalDate oneWeekAgo = java.time.LocalDate.now().minusDays(7);
        List<Book> books = bookRepository.findTop10MostBorrowedSince(oneWeekAgo, PageRequest.of(0, 10));
        return books.stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookResponse createBook(CreateBookRequest request) {
        Book book = bookMapper.toBook(request);
        book = bookRepository.save(book);
        return bookMapper.toBookResponse(book);
    }

    @Override
    public BookResponse updateBook(Long bookId, UpdateBookRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        bookMapper.updateBookFromRequest(request, book);
        book = bookRepository.save(book);

        return bookMapper.toBookResponse(book);
    }

    @Override
    public BookResponse getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        return bookMapper.toBookResponse(book);
    }

    @Override
    public void deleteBook(Long bookId) {
        bookRepository.deleteById(bookId);
    }

}
