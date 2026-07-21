package com.library.service.impl;

import com.library.dto.request.CreateBookRequest;
import com.library.dto.request.UpdateBookRequest;
import com.library.dto.response.BookResponse;
import com.library.dto.response.PageResponse;
import com.library.dto.response.TopBookProjection;
import com.library.entity.Book;
import com.library.repository.BookRepository;
import com.library.repository.BookCopyRepository;
import com.library.mapper.BookMapper;
import com.library.service.interfaces.BookService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    final BookCopyRepository bookCopyRepository;
    final BookMapper bookMapper;

    private BookResponse mapToResponse(Book book) {
        BookResponse response = bookMapper.toBookResponse(book);
        long available = bookCopyRepository.countByBook_IdAndStatus(book.getId(),
                com.library.entity.enums.BookCopyStatus.AVAILABLE);
        response.setAvailableCopiesCount(available);
        return response;
    }

    @Override
    public PageResponse<BookResponse> getAllBooks(Long id, String title, String author, String category,
            String publisher, String isbn, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
                
        String titlePattern = (title != null && !title.trim().isEmpty()) ? "%" + title.trim().toLowerCase() + "%" : null;
        String authorPattern = (author != null && !author.trim().isEmpty()) ? "%" + author.trim().toLowerCase() + "%" : null;
        String categoryPattern = (category != null && !category.trim().isEmpty()) ? "%" + category.trim().toLowerCase() + "%" : null;
        String publisherPattern = (publisher != null && !publisher.trim().isEmpty()) ? "%" + publisher.trim().toLowerCase() + "%" : null;
        String isbnPattern = (isbn != null && !isbn.trim().isEmpty()) ? "%" + isbn.trim().toLowerCase() + "%" : null;

        Page<Book> bookPage = bookRepository.findByFilters(id, titlePattern, authorPattern, categoryPattern, publisherPattern, isbnPattern, pageable);

        List<BookResponse> content = bookPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<BookResponse>builder()
                .content(content)
                .pageNumber(bookPage.getNumber())
                .pageSize(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .build();
    }

    @Override
    public List<BookResponse> getPopularBooks() {
        java.time.LocalDate oneWeekAgo = java.time.LocalDate.now().minusDays(7);
        List<Book> books = bookRepository.findTop10MostBorrowedSince(oneWeekAgo, PageRequest.of(0, 10));
        return books.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopBookProjection> getTop10MostBorrowedBooks() {
        return bookRepository.getTop10MostBorrowedBooks();
    }

    @Override
    public BookResponse createBook(CreateBookRequest request) {
        Book book = bookMapper.toBook(request);
        book = bookRepository.save(book);
        return mapToResponse(book);
    }

    @Override
    public BookResponse updateBook(Long bookId, UpdateBookRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        bookMapper.updateBookFromRequest(request, book);
        book = bookRepository.save(book);

        return mapToResponse(book);
    }

    @Override
    public BookResponse getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        return mapToResponse(book);
    }

    @Override
    public void deleteBook(Long bookId) {
        bookRepository.deleteById(bookId);
    }

    @Override
    public List<String> getUniqueCategories() {
        return bookRepository.findUniqueCategories();
    }

}
