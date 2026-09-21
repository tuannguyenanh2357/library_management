package com.library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingRequestCreationRequest {
    @NotNull(message = "ID thành viên không được để trống")
    private Long memberId;
    
    @NotNull(message = "ID sách không được để trống")
    private Long bookId;

    @NotNull(message = "Ngày hết hạn dự kiến không được để trống")
    private LocalDate expectedDueDate;

    private String notes;
}
