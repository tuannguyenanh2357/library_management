package com.library.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.library.service.interfaces.MemberService;
import com.library.repository.MemberRepository;
import com.library.mapper.MemberMapper;
import com.library.dto.response.MemberResponse;
import com.library.dto.request.MemberCreationRequest;
import com.library.dto.request.MemberUpdateRequest;
import com.library.entity.Member;
import com.library.entity.enums.MemberRole;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.exception.MemberNotFoundException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
public class MemberServiceImpl implements MemberService {
    final MemberRepository memberRepository;
    final MemberMapper memberMapper;
    final PasswordEncoder passwordEncoder;

    @Override
    public List<MemberResponse> getAllMembers() {
        List<Member> members = memberRepository.findAll();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            boolean hasAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean hasLibrarian = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_LIBRARIAN"));

            // Nếu người dùng là LIBRARIAN và không phải ADMIN -> Chỉ được xem danh sách Độc giả (MEMBER)
            if (!hasAdmin && hasLibrarian) {
                members = members.stream()
                        .filter(m -> m.getRole() == MemberRole.MEMBER)
                        .collect(Collectors.toList());
            }
        }

        return members.stream()
                .map(memberMapper::toMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MemberResponse getMemberById(Long memberID) {
        Member member = memberRepository.findById(memberID)
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));
        return memberMapper.toMemberResponse(member);
    }

    @Override
    public MemberResponse createMember(MemberCreationRequest request) {
        Member member = memberMapper.toMember(request);
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member = memberRepository.save(member);
        return memberMapper.toMemberResponse(member);
    }

    @Override
    public MemberResponse updateMember(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            boolean hasAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean hasLibrarian = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_LIBRARIAN"));

            if (!hasAdmin && hasLibrarian && member.getRole() != MemberRole.MEMBER) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Thủ thư chỉ có quyền cập nhật thông tin tài khoản Độc giả (MEMBER).");
            }
        }

        String newPassword = request.getPassword();
        memberMapper.updateMemberFromRequest(request, member);
        if (newPassword != null && !newPassword.isBlank()) {
            member.setPassword(passwordEncoder.encode(newPassword));
        }
        
        member = memberRepository.save(member);
        return memberMapper.toMemberResponse(member);
    }

    @Override
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));
        
        if (member.getRole() == MemberRole.ADMIN) {
            throw new AppException(ErrorCode.CANNOT_DELETE_ADMIN);
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            boolean hasAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!hasAdmin) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Chỉ Quản trị viên (ADMIN) mới có quyền xóa tài khoản.");
            }
        }

        if (member.hasBorrowedBooks()) {
            throw new AppException(ErrorCode.CANNOT_DELETE_MEMBER_WITH_BOOKS);
        }
        
        memberRepository.delete(member);
    }

    @Override
    public MemberResponse getMemberByUsername(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy thành viên với tên người dùng: " + username));
        return memberMapper.toMemberResponse(member);
    }

    @Override
    public MemberResponse updateMyProfile(String username, com.library.dto.request.MyProfileUpdateRequest request) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));
        
        if (request.getName() != null) member.setName(request.getName());
        if (request.getEmail() != null) member.setEmail(request.getEmail());
        if (request.getPhone() != null) member.setPhone(request.getPhone());
        if (request.getAddress() != null) member.setAddress(request.getAddress());
        if (request.getAvatar() != null) member.setAvatar(request.getAvatar());

        member = memberRepository.save(member);
        return memberMapper.toMemberResponse(member);
    }

    @Override
    public void changePassword(String username, com.library.dto.request.ChangePasswordRequest request) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));
        
        if (!passwordEncoder.matches(request.getOldPassword(), member.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }
        
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);
    }

    @Override
    public List<com.library.dto.response.UnpaidMemberProjection> getMembersWithUnpaidFines() {
        return memberRepository.getMembersWithUnpaidFines();
    }
}
