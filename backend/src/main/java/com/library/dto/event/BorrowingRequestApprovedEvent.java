package com.library.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingRequestApprovedEvent implements Serializable {
    private Long requestId;
    private String memberEmail;
    private String memberName;
    private String bookTitle;
    private LocalDate expectedDueDate;
}
