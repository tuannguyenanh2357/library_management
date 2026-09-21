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
    @NotNull(message = "ID thành viên không được để trống")
    Long memberId;
    
    @NotNull(message = "ID sách không được để trống")
    Long bookId;
}
