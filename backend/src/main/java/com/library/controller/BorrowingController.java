package com.library.controller;

import com.library.dto.response.BorrowingResponse;
import com.library.dto.request.BorrowingCreationRequest;
import com.library.service.interfaces.BorrowingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrowings")
@AllArgsConstructor
public class BorrowingController {
    private final BorrowingService borrowingService;

    @GetMapping
    public ResponseEntity<List<BorrowingResponse>> getAllBorrowings() {
        return ResponseEntity.ok(borrowingService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowingResponse> getBorrowingById(@PathVariable Long id) {
        return ResponseEntity.ok(borrowingService.getById(id));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<BorrowingResponse>> getBorrowingsByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(borrowingService.getByMemberId(memberId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowingResponse>> getOverdueBorrowings() {
        return ResponseEntity.ok(borrowingService.getOverdueBorrowings());
    }

    @PostMapping
    public ResponseEntity<BorrowingResponse> borrowBook(@RequestBody BorrowingCreationRequest request) {
        return ResponseEntity.ok(borrowingService.borrowBook(request));
    }

    @PutMapping("/{borrowingId}/return")
    public ResponseEntity<BorrowingResponse> returnBook(@PathVariable Long borrowingId) {
        return ResponseEntity.ok(borrowingService.returnBook(borrowingId));
    }



}
