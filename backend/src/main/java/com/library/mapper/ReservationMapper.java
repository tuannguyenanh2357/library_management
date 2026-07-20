package com.library.mapper;

import com.library.dto.response.ReservationResponse;
import com.library.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberName", source = "member.name")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "bookCover", source = "book.imageUrl")
    @Mapping(target = "fulfilledCopyId", source = "fulfilledCopy.id")
    @Mapping(target = "fulfilledCopyBarcode", source = "fulfilledCopy.barCode")
    ReservationResponse toResponse(Reservation reservation);
}
