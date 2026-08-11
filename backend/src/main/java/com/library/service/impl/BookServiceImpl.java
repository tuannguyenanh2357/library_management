package com.library.service.impl;

import com.library.dto.request.CreateBookRequest;
import com.library.dto.request.UpdateBookRequest;
import com.library.dto.response.BookResponse;
import com.library.dto.response.PageResponse;
import com.library.dto.response.TopBookResponse;
import com.library.entity.Book;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.BookCopyRepository;
import com.library.mapper.BookMapper;
import com.library.service.interfaces.BookService;
import com.library.entity.enums.BookCopyStatus;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookServiceImpl implements BookService {
        BookRepository bookRepository;
        BookCopyRepository bookCopyRepository;
        BookMapper bookMapper;

        BookResponse mapToResponse(Book book) {
                BookResponse response = bookMapper.toBookResponse(book);
                long available = bookCopyRepository.countByBookIdAndStatus(book.getId(), BookCopyStatus.AVAILABLE);
                response.setAvailableCopiesCount(available);
                return response;
        }

        @Override
        @Cacheable(value = "books", key = "{#id, #title, #author, #category, #publisher, #isbn, #page, #size}")
        public PageResponse<BookResponse> getAllBooks(Long id, String title, String author, String category,
                        String publisher, String isbn, int page, int size) {
                Pageable pageable = PageRequest.of(page, size,
                                Sort.by(Sort.Direction.DESC, "createdAt"));

                String titlePattern = (title != null && !title.trim().isEmpty())
                                ? "%" + title.trim().toLowerCase() + "%"
                                : null;

                String authorPattern = (author != null && !author.trim().isEmpty())
                                ? "%" + author.trim().toLowerCase() + "%"
                                : null;

                String categoryPattern = (category != null && !category.trim().isEmpty())
                                ? "%" + category.trim().toLowerCase() + "%"
                                : null;

                String publisherPattern = (publisher != null && !publisher.trim().isEmpty())
                                ? "%" + publisher.trim().toLowerCase() + "%"
                                : null;

                String isbnPattern = (isbn != null && !isbn.trim().isEmpty()) ? "%" + isbn.trim().toLowerCase() + "%"
                                : null;

                Page<Book> bookPage = bookRepository.findByFilters(id, titlePattern, authorPattern, categoryPattern,
                                publisherPattern, isbnPattern, pageable);

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
        @Cacheable(value = "topBooks")
        public List<TopBookResponse> getTop10MostBorrowedBooks() {
                return bookRepository.getTop10MostBorrowedBooks().stream()
                                .map(p -> new TopBookResponse(p.getBookId(), p.getTitle(), p.getAuthor(),
                                                p.getCategory(), p.getImageUrl(), p.getBorrowCount()))
                                .collect(Collectors.toList());
        }

        @Override
        @CacheEvict(value = { "books", "topBooks", "categories" }, allEntries = true)
        @Transactional(rollbackFor = Exception.class)
        public BookResponse createBook(CreateBookRequest request) {
                Book book = bookMapper.toBook(request);
                book = bookRepository.save(book);
                return mapToResponse(book);
        }

        @Override
        @CacheEvict(value = { "books", "topBooks", "categories" }, allEntries = true)
        @Transactional(rollbackFor = Exception.class)
        public BookResponse updateBook(Long bookId, UpdateBookRequest request) {
                Book book = bookRepository.findById(bookId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_NOT_FOUND));

                bookMapper.updateBookFromRequest(request, book);
                book = bookRepository.save(book);

                return mapToResponse(book);
        }

        @Override
        @Cacheable(value = "books", key = "#bookId")
        public BookResponse getBookById(Long bookId) {
                Book book = bookRepository.findById(bookId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_NOT_FOUND));
                return mapToResponse(book);
        }

        @Override
        @CacheEvict(value = { "books", "topBooks", "categories" }, allEntries = true)
        @Transactional(rollbackFor = Exception.class)
        public void deleteBook(Long bookId) {
                Book book = bookRepository.findById(bookId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_NOT_FOUND));

                if (!book.getCopies().isEmpty()) {
                        throw new AppException(ErrorCode.INVALID_REQUEST,
                                        "Không thể xóa đầu sách này vì vẫn còn bản sao trong hệ thống. Vui lòng xóa hoặc xử lý các bản sao trước.");
                }

                bookRepository.delete(book);
        }

        @Override
        @Cacheable(value = "categories")
        public List<String> getUniqueCategories() {
                return bookRepository.findUniqueCategories();
        }

}
