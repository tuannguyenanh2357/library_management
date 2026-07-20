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
    @NotBlank(message = "Barcode is required")
    private String barcode;

    @NotNull(message = "Due Date is required")
    private LocalDate dueDate;
}
