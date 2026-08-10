package com.library.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.library.service.interfaces.BookCopyService;
import com.library.dto.response.BookCopyResponse;
import com.library.dto.request.BookCopyCreationRequest;
import com.library.dto.request.BookCopyUpdateRequest;
import jakarta.validation.Valid;
import java.net.URI;
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
    public ResponseEntity<List<BookCopyResponse>> getAllBookCopies() {
        return ResponseEntity.ok(bookCopyService.getAllBookCopies());
    }

    @PostMapping
    public ResponseEntity<BookCopyResponse> createBookCopy(@Valid @RequestBody BookCopyCreationRequest request) {
        BookCopyResponse created = bookCopyService.createBookCopy(request);
        URI location = URI.create("/book-copies/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookCopyResponse> updateBookCopy(@PathVariable Long id, @Valid @RequestBody BookCopyUpdateRequest request) {
        BookCopyResponse updated = bookCopyService.updateBookCopy(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookCopy(@PathVariable Long id) {
        bookCopyService.deleteBookCopy(id);
        return ResponseEntity.noContent().build();
    }
}
