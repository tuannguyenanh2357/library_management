package com.library.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.util.List;
import java.util.stream.Collectors;

import com.library.service.interfaces.MemberService;
import com.library.repository.MemberRepository;
import com.library.mapper.MemberMapper;
import com.library.dto.response.MemberResponse;
import com.library.dto.request.MemberCreationRequest;
import com.library.dto.request.MemberUpdateRequest;
import com.library.entity.Member;
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
        
        if (member.hasBorrowedBooks()) {
            throw new RuntimeException("Không thể xóa độc giả vì họ đang mượn sách chưa trả.");
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
            throw new RuntimeException("Mật khẩu cũ không chính xác");
        }
        
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);
    }
}
