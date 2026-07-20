package com.library.controller;

import com.library.dto.response.FineResponse;
import com.library.service.interfaces.FineService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/fines")
@AllArgsConstructor
public class FineController {
    private final FineService fineService;
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<FineResponse>> getUnpaidFines() {
        return ResponseEntity.ok(fineService.getUnpaidFines());
    }


    @PutMapping("/{fineId}/pay")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<FineResponse> payFine(@PathVariable Long fineId) {
        return ResponseEntity.ok(fineService.payFine(fineId));
    }
}
