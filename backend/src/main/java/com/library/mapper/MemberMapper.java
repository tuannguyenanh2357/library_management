package com.library.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.library.dto.response.MemberResponse;
import com.library.dto.request.MemberCreationRequest;
import com.library.dto.request.MemberUpdateRequest;
import com.library.entity.Member;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    Member toMember(MemberCreationRequest request);

    MemberResponse toMemberResponse(Member member);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMemberFromRequest(MemberUpdateRequest request, @MappingTarget Member member);

}