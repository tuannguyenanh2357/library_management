package com.library.mapper;

import com.library.dto.response.FineResponse;
import com.library.entity.Fines;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FineMapper {

    @Mapping(target = "memberId", source = "borrowing.member.id")
    @Mapping(target = "memberName", source = "borrowing.member.name")
    @Mapping(target = "borrowingId", source = "borrowing.id")
    @Mapping(target = "bookTitle", source = "borrowing.bookCopy.book.title")
    FineResponse toResponse(Fines fine);

}