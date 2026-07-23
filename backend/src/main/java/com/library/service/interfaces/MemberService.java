package com.library.service.interfaces;

import com.library.dto.response.MemberResponse;
import com.library.dto.request.MemberCreationRequest;
import com.library.dto.request.MemberUpdateRequest;
import com.library.entity.Member;
import com.library.dto.response.UnpaidMemberProjection;
import java.util.List;

public interface MemberService {
    MemberResponse createMember(MemberCreationRequest request);
    MemberResponse updateMember(Long memberId, MemberUpdateRequest request);
    MemberResponse getMemberById(Long memberId);

    List<MemberResponse> getAllMembers();
    void deleteMember(Long memberId);
    MemberResponse getMemberByUsername(String username);

    MemberResponse updateMyProfile(String username, com.library.dto.request.MyProfileUpdateRequest request);
    void changePassword(String username, com.library.dto.request.ChangePasswordRequest request);

    List<UnpaidMemberProjection> getMembersWithUnpaidFines();
}
