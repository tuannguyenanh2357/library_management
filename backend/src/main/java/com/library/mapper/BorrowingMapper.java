package com.library.mapper;

import com.library.dto.response.BorrowingResponse;
import com.library.entity.Borrowing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BorrowingMapper {

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberName", source = "member.name")

    @Mapping(target = "barCode", source = "bookCopy.barCode")
    @Mapping(target = "bookCopy", source = "bookCopy.id")

    @Mapping(target = "bookId", source = "bookCopy.id")
    @Mapping(target = "bookTitle", source = "bookCopy.book.title")

    BorrowingResponse toResponse(Borrowing borrowing);

}
