package com.library.dto.response;

import com.library.entity.enums.ReservationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationResponse {
    Long id;
    Long memberId;
    String memberName;
    Long bookId;
    String bookTitle;
    String bookCover;
    Long fulfilledCopyId;
    String fulfilledCopyBarcode;
    ReservationStatus status;
    LocalDateTime requestDate;
    LocalDateTime fulfilledDate;
    LocalDateTime expiryDate;
    LocalDate expectedAvailableDate;
}
