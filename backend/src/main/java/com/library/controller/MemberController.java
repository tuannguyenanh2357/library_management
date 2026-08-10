package com.library.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.library.service.interfaces.MemberService;
import com.library.dto.response.MemberResponse;
import com.library.dto.request.MemberCreationRequest;
import com.library.dto.request.MemberUpdateRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RequestMapping("/members")
@RestController
@AllArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberCreationRequest request) {
        MemberResponse created = memberService.createMember(request);
        URI location = URI.create("/members/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<MemberResponse> updateMember(@PathVariable Long id,
            @Valid @RequestBody MemberUpdateRequest request) {
        MemberResponse updated = memberService.updateMember(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return ResponseEntity.status(401).body("Authentication object is null");
            }
            String username = auth.getName();
            return ResponseEntity.ok(memberService.getMemberByUsername(username));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/me")
    public ResponseEntity<MemberResponse> updateMyProfile(
            @Valid @RequestBody com.library.dto.request.MyProfileUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        MemberResponse updated = memberService.updateMyProfile(username, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody com.library.dto.request.ChangePasswordRequest request) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            memberService.changePassword(username, request);
            return ResponseEntity.ok(java.util.Map.of("message", "Đổi mật khẩu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/unpaid-fines")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<com.library.dto.response.UnpaidMemberProjection>> getMembersWithUnpaidFines() {
        return ResponseEntity.ok(memberService.getMembersWithUnpaidFines());
    }

    @GetMapping("/me/favorites")
    public ResponseEntity<List<com.library.dto.response.BookResponse>> getMyFavoriteBooks() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(memberService.getFavoriteBooks(username));
    }

    @PostMapping("/me/favorites/{bookId}")
    public ResponseEntity<?> addFavoriteBook(@PathVariable Long bookId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        memberService.addFavoriteBook(username, bookId);
        return ResponseEntity.ok(java.util.Map.of("message", "Thêm sách vào danh sách yêu thích thành công"));
    }

    @DeleteMapping("/me/favorites/{bookId}")
    public ResponseEntity<?> removeFavoriteBook(@PathVariable Long bookId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        memberService.removeFavoriteBook(username, bookId);
        return ResponseEntity.ok(java.util.Map.of("message", "Đã xóa sách khỏi danh sách yêu thích"));
    }
}