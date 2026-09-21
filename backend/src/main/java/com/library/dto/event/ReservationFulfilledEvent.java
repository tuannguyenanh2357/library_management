package com.library.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationFulfilledEvent {
    private String memberEmail;
    private String memberName;
    private String bookTitle;
    private LocalDateTime expiryDate;
}
