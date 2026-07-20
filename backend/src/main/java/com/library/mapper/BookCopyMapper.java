package com.library.mapper;

import com.library.dto.request.BookCopyCreationRequest;
import com.library.dto.request.BookCopyUpdateRequest;
import com.library.dto.response.BookCopyResponse;
import com.library.entity.BookCopy;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookCopyMapper {

    @Mapping(target = "book.id", source = "bookId")
    BookCopy toBookCopy(BookCopyCreationRequest request);

    @Mapping(target = "author", source = "book.author")
    @Mapping(target = "title", source = "book.title")
    @Mapping(target = "bookId", source = "book.id")
    BookCopyResponse toResponse(BookCopy entity);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "book.id", source = "bookId")
    void updateBookCopy(
            @MappingTarget BookCopy entity,
            BookCopyUpdateRequest request
    );

}