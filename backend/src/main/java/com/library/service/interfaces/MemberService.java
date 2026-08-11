package com.library.service.interfaces;

import com.library.dto.response.MemberResponse;
import com.library.dto.request.ChangePasswordRequest;
import com.library.dto.request.MemberCreationRequest;
import com.library.dto.request.MemberUpdateRequest;
import com.library.dto.request.MyProfileUpdateRequest;
import com.library.dto.response.UnpaidMemberProjection;
import java.util.List;

public interface MemberService {
    MemberResponse createMember(MemberCreationRequest request);

    MemberResponse updateMember(Long memberId, MemberUpdateRequest request);

    MemberResponse getMemberById(Long memberId);

    List<MemberResponse> getAllMembers();

    void deleteMember(Long memberId);

    MemberResponse getMemberByUsername(String username);

    MemberResponse updateMyProfile(String username, MyProfileUpdateRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    List<UnpaidMemberProjection> getMembersWithUnpaidFines();

    // void addFavoriteBook(String username, Long bookId);

    // void removeFavoriteBook(String username, Long bookId);

    // List<BookResponse> getFavoriteBooks(String username);
}
