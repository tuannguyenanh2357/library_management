package com.library.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.library.service.interfaces.BookCopyService;
import com.library.dto.response.BookCopyResponse;
import com.library.dto.request.BookCopyCreationRequest;
import com.library.dto.request.BookCopyUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;

@RequestMapping("/book-copies")
@RestController
@AllArgsConstructor
public class BookCopyController {
    private final BookCopyService bookCopyService;

    @GetMapping("/{id}")
    public ResponseEntity<BookCopyResponse> getBookCopyById(@PathVariable Long id) {
        return ResponseEntity.ok(bookCopyService.getBookCopyById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<BookCopyResponse>> getAllBookCopies() {
        return ResponseEntity.ok(bookCopyService.getAllBookCopies());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BookCopyResponse> createBookCopy(@Valid @RequestBody BookCopyCreationRequest request) {
        BookCopyResponse created = bookCopyService.createBookCopy(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BookCopyResponse> updateBookCopy(@PathVariable Long id,
            @Valid @RequestBody BookCopyUpdateRequest request) {
        BookCopyResponse updated = bookCopyService.updateBookCopy(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<BookCopyResponse> getBookCopyByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(bookCopyService.getBookCopyByBarcode(barcode));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<Void> deleteBookCopy(@PathVariable Long id) {
        bookCopyService.deleteBookCopy(id);
        return ResponseEntity.noContent().build();
    }
}
