package com.library.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.library.security.SecurityUtils;
import com.library.service.interfaces.MemberService;
import com.library.dto.response.MemberResponse;
import com.library.dto.response.MessageResponse;
import com.library.dto.response.UnpaidMemberProjection;
import com.library.dto.request.MemberCreationRequest;
import com.library.dto.request.MemberUpdateRequest;
import com.library.dto.request.MyProfileUpdateRequest;
import com.library.dto.request.ChangePasswordRequest;
import jakarta.validation.Valid;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberCreationRequest request) {
        MemberResponse created = memberService.createMember(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
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
    public ResponseEntity<MemberResponse> getMyProfile() {
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(memberService.getMemberByUsername(username));
    }

    @PutMapping("/me")
    public ResponseEntity<MemberResponse> updateMyProfile(
            @Valid @RequestBody MyProfileUpdateRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        MemberResponse updated = memberService.updateMyProfile(username, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword( @Valid @RequestBody ChangePasswordRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        memberService.changePassword(username, request);
        return ResponseEntity.ok(MessageResponse.of("Đổi mật khẩu thành công"));
    }

    @GetMapping("/unpaid-fines")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<UnpaidMemberProjection>> getMembersWithUnpaidFines() {
        return ResponseEntity.ok(memberService.getMembersWithUnpaidFines());
    }
}