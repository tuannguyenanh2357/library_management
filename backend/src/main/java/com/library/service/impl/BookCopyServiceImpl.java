package com.library.service.impl;

import com.library.entity.enums.BookCopyStatus;

import com.library.dto.request.BookCopyCreationRequest;
import com.library.dto.request.BookCopyUpdateRequest;
import com.library.dto.response.BookCopyResponse;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.mapper.BookCopyMapper;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.service.interfaces.BookCopyService;
import com.library.service.interfaces.ReservationService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class BookCopyServiceImpl implements BookCopyService {
    BookCopyRepository bookCopyRepository;
    BookRepository bookRepository;
    BookCopyMapper bookCopyMapper;
    ReservationService reservationService;

    @Override
    public List<BookCopyResponse> getAllBookCopies() {
        return bookCopyRepository.findAllWithBook().stream()
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
    public BookCopyResponse getBookCopyByBarcode(String barcode) {
        BookCopy bookCopy = bookCopyRepository.findByBarCode(barcode)
                .orElseThrow(() -> new RuntimeException("Book copy not found with barcode: " + barcode));
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
        
        // Kiểm tra xem có ai đang xếp hàng đợi cuốn sách này không. Nếu có thì gán luôn bản sao này cho người đó.
        reservationService.fulfillNextReservationIfAny(book.getId(), savedBookCopy);
        
        return bookCopyMapper.toResponse(savedBookCopy);
    }

    @Override
    public BookCopyResponse updateBookCopy(Long bookCopyId, BookCopyUpdateRequest request) {
        BookCopy existingBookCopy = bookCopyRepository.findById(bookCopyId)
                .orElseThrow(() -> new RuntimeException("Book copy not found"));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (existingBookCopy.getStatus() == BookCopyStatus.BORROWED) {
            throw new RuntimeException("Không thể cập nhật trạng thái của bản sao đang được mượn.");
        }

        bookCopyMapper.updateBookCopy(existingBookCopy, request);
        existingBookCopy.setBook(book);

        BookCopy updatedBookCopy = bookCopyRepository.save(existingBookCopy);
        return bookCopyMapper.toResponse(updatedBookCopy);
    }

    @Override
    public void deleteBookCopy(Long bookCopyId) {
        BookCopy existingBookCopy = bookCopyRepository.findById(bookCopyId)
                .orElseThrow(() -> new RuntimeException("Book copy not found"));

        if (existingBookCopy.getStatus() == BookCopyStatus.BORROWED) {
            throw new RuntimeException("Không thể xóa bản sao đang được mượn.");
        }

        bookCopyRepository.deleteById(bookCopyId);
    }
}
