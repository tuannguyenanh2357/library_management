package com.library.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.util.List;
import java.util.stream.Collectors;
import com.library.security.SecurityUtils;
import com.library.service.interfaces.MemberService;
import com.library.repository.MemberRepository;
import com.library.mapper.MemberMapper;

import com.library.dto.response.MemberResponse;
import com.library.dto.response.UnpaidMemberProjection;
import com.library.dto.request.MemberCreationRequest;
import com.library.dto.request.MemberUpdateRequest;

import com.library.entity.Member;
import com.library.entity.enums.MemberRole;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.exception.MemberNotFoundException;
import com.library.dto.request.MyProfileUpdateRequest;
import com.library.dto.request.ChangePasswordRequest;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MemberServiceImpl implements MemberService {
    MemberRepository memberRepository;
    MemberMapper memberMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public List<MemberResponse> getAllMembers() {
        List<Member> members = memberRepository.findAll();

        // Nếu là thử thư và không phải ADMIN Chỉ được xem danh sách Độc giả
        if (!SecurityUtils.isAdmin() && SecurityUtils.isLibrarian()) {
            members = members.stream()
                    .filter(m -> m.getRole() == MemberRole.MEMBER)
                    .collect(Collectors.toList());
        }

        return members.stream()
                .map(memberMapper::toMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MemberResponse getMemberById(Long memberID) {
        return memberMapper.toMemberResponse(getMemberByIdOrThrow(memberID));
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
        Member member = getMemberByIdOrThrow(memberId);

        if (!SecurityUtils.isAdmin() && SecurityUtils.isLibrarian() && member.getRole() != MemberRole.MEMBER) {
            throw new AppException(ErrorCode.UNAUTHORIZED,
                    "Thủ thư chỉ có quyền cập nhật thông tin tài khoản Độc giả (MEMBER).");
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
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = getMemberByIdOrThrow(memberId);

        if (member.getRole() == MemberRole.ADMIN) {
            throw new AppException(ErrorCode.CANNOT_DELETE_ADMIN);
        }

        if (!SecurityUtils.isAdmin()) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Chỉ Quản trị viên (ADMIN) mới có quyền xóa tài khoản.");
        }

        if (member.hasBorrowedBooks()) {
            throw new AppException(ErrorCode.CANNOT_DELETE_MEMBER_WITH_BOOKS);
        }

        memberRepository.delete(member);
    }

    @Override
    public MemberResponse getMemberByUsername(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(
                        () -> new MemberNotFoundException("Không tìm thấy thành viên với tên người dùng: " + username));
        return memberMapper.toMemberResponse(member);
    }

    @Override
    public MemberResponse updateMyProfile(String username, MyProfileUpdateRequest request) {
        Member member = getMemberByUsernameOrThrow(username);

        if (request.getName() != null)
            member.setName(request.getName());
        if (request.getEmail() != null)
            member.setEmail(request.getEmail());
        if (request.getPhone() != null)
            member.setPhone(request.getPhone());
        if (request.getAddress() != null)
            member.setAddress(request.getAddress());

        member = memberRepository.save(member);
        return memberMapper.toMemberResponse(member);
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        Member member = getMemberByUsernameOrThrow(username);

        if (!passwordEncoder.matches(request.getOldPassword(), member.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);
    }

    @Override
    public List<UnpaidMemberProjection> getMembersWithUnpaidFines() {
        return memberRepository.getMembersWithUnpaidFines();
    }

    private Member getMemberByIdOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));
    }

    private Member getMemberByUsernameOrThrow(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));
    }

}
