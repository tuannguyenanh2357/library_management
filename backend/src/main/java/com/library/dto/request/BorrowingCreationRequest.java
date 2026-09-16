package com.library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BorrowingCreationRequest {
    @NotNull(message = "ID thành viên không được để trống")
    private Long memberId;
    @NotNull(message = "ID bản sao sách không được để trống")
    private Long bookCopyId;
    @NotNull(message = "Ngày hết hạn không được để trống")
    private LocalDate dueDate;

    String note;

}
