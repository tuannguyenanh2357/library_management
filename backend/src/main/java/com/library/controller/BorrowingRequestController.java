package com.library.controller;

import com.library.dto.request.BorrowingRequestCreationRequest;
import com.library.dto.response.BorrowingRequestResponse;
import com.library.service.interfaces.BorrowingRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrowing-requests")
@RequiredArgsConstructor
public class BorrowingRequestController {

    private final BorrowingRequestService service;

    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BorrowingRequestResponse> createRequest(
            @Valid @RequestBody BorrowingRequestCreationRequest request) {
        return ResponseEntity.ok(service.createRequest(request));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<BorrowingRequestResponse>> getPendingRequests() {
        return ResponseEntity.ok(service.getPendingRequests());
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<BorrowingRequestResponse>> getHistoryRequests() {
        return ResponseEntity.ok(service.getHistoryRequests());
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or principal.claims['memberId'] == #memberId")
    public ResponseEntity<List<BorrowingRequestResponse>> getRequestsByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(service.getRequestsByMember(memberId));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BorrowingRequestResponse> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(service.approveRequest(id, null));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BorrowingRequestResponse> rejectRequest(
            @PathVariable Long id,
            @RequestParam String reason) {
        return ResponseEntity.ok(service.rejectRequest(id, reason));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BorrowingRequestResponse> cancelRequest(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return ResponseEntity.ok(service.cancelRequest(id, username));
    }
}
