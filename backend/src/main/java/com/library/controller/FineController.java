package com.library.controller;

import com.library.dto.request.FineUpdateRequest;
import com.library.dto.response.FineResponse;
import com.library.service.interfaces.FineService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/fines")
@AllArgsConstructor
public class FineController {
    private final FineService fineService;
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<FineResponse>> getAll() {
        return ResponseEntity.ok(fineService.getAll());
    }

    @GetMapping("/{fineId}")
    public ResponseEntity<FineResponse> getFineById(@PathVariable Long fineId) {
        return ResponseEntity.ok(fineService.getById(fineId));
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or principal.claims['memberId'] == #memberId")
    public ResponseEntity<List<FineResponse>> getFinesByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(fineService.getByMemberId(memberId));
    }

    @GetMapping("/unpaid")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<FineResponse>> getUnpaidFines() {
        return ResponseEntity.ok(fineService.getUnpaidFines());
    }


    @PutMapping("/{fineId}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<FineResponse> payFine(@PathVariable Long fineId) {
        return ResponseEntity.ok(fineService.payFine(fineId));
    }

    @PutMapping("/{fineId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<FineResponse> cancelFine(@PathVariable Long fineId, @Valid @RequestBody FineUpdateRequest request) {
        return ResponseEntity.ok(fineService.cancelFine(fineId, request.getReason()));
    }
}
