package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class BorrowingRequestApprovalRequest {
    @NotBlank(message = "Barcode không được để trống")
    private String barcode;

    @NotNull(message = "Ngày hết hạn không được để trống")
    private LocalDate dueDate;
}
