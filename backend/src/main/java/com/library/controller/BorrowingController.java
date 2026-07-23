package com.library.controller;

import com.library.dto.response.BorrowingResponse;
import com.library.dto.request.BorrowingCreationRequest;
import com.library.service.interfaces.BorrowingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/borrowings")
@AllArgsConstructor
public class BorrowingController {
    private final BorrowingService borrowingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<BorrowingResponse>> getAllBorrowings() {
        return ResponseEntity.ok(borrowingService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BorrowingResponse> getBorrowingById(@PathVariable Long id) {
        return ResponseEntity.ok(borrowingService.getById(id));
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or principal.claims['memberId'] == #memberId")
    public ResponseEntity<List<BorrowingResponse>> getBorrowingsByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(borrowingService.getByMemberId(memberId));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<BorrowingResponse>> getOverdueBorrowings() {
        return ResponseEntity.ok(borrowingService.getOverdueBorrowings());
    }

    @GetMapping("/sp-overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<com.library.dto.response.OverdueBookProjection>> getOverdueBooksFromSP() {
        return ResponseEntity.ok(borrowingService.getOverdueBooksFromSP());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BorrowingResponse> borrowBook(@RequestBody BorrowingCreationRequest request) {
        return ResponseEntity.ok(borrowingService.borrowBook(request));
    }

    @PutMapping("/{borrowingId}/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BorrowingResponse> returnBook(@PathVariable Long borrowingId) {
        return ResponseEntity.ok(borrowingService.returnBook(borrowingId));
    }

    @GetMapping("/copy/{copyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<BorrowingResponse>> getBorrowingsByCopyId(@PathVariable Long copyId) {
        return ResponseEntity.ok(borrowingService.getByCopyId(copyId));
    }

    @PutMapping("/{borrowingId}/renew")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BorrowingResponse> renewBorrowing(@PathVariable Long borrowingId) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(borrowingService.renewBorrowing(borrowingId, username));
    }

    @PutMapping("/{borrowingId}/report-lost")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BorrowingResponse> reportLost(@PathVariable Long borrowingId) {
        return ResponseEntity.ok(borrowingService.reportLost(borrowingId));
    }

    @PutMapping("/{borrowingId}/report-damaged")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BorrowingResponse> reportDamaged(@PathVariable Long borrowingId) {
        return ResponseEntity.ok(borrowingService.reportDamaged(borrowingId));
    }

}
