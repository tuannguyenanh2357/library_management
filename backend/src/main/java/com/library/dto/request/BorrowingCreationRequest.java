package com.library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BorrowingCreationRequest {
    @NotNull (message = "Member ID is required")
    private Long memberId;
    @NotNull (message = "Book Copy ID is required")
    private Long bookCopyId;
    @NotNull (message = "Due Date is required")
    private LocalDate dueDate;
}
