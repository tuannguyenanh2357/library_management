package com.library.controller;

import com.library.dto.response.FineResponse;
import com.library.service.interfaces.FineService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fines")
@AllArgsConstructor
public class FineController {
    private final FineService fineService;
    @GetMapping
    public ResponseEntity<List<FineResponse>> getAll() {
        return ResponseEntity.ok(fineService.getAll());
    }

    @GetMapping("/fineId")
    public ResponseEntity<FineResponse> getFineById(@PathVariable Long fineId) {
        return ResponseEntity.ok(fineService.getById(fineId));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<FineResponse>> getFinesByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(fineService.getByMemberId(memberId));
    }

    @GetMapping("/unpaid")
    public ResponseEntity<List<FineResponse>> getUnpaidFines() {
        return ResponseEntity.ok(fineService.getUnpaidFines());
    }

    @PutMapping("/{fineId}")
    public ResponseEntity<FineResponse> updateFine(@PathVariable Long fineId, @RequestBody FineResponse fineResponse) {
        return ResponseEntity.ok(fineService.payFine(fineId));
    }
}
