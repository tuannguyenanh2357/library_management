package com.library.controller;

import com.library.dto.request.ReservationCreationRequest;
import com.library.dto.response.ReservationResponse;
import com.library.service.interfaces.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.library.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationCreationRequest request) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<List<ReservationResponse>> getMyReservations() {
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(reservationService.getMyReservations(username));
    }

    @GetMapping("/book/{bookId}/pending")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getPendingReservationsForBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(reservationService.getPendingReservationsForBook(bookId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername();
        reservationService.cancelReservation(id, username);
        return ResponseEntity.ok().build();
    }
}
