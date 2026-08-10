package com.library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationCreationRequest {
    @NotNull(message = "Member ID is required")
    Long memberId;
    
    @NotNull(message = "Book ID is required")
    Long bookId;
}
