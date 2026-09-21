package com.library.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingRequestRejectedEvent implements Serializable {
    private Long requestId;
    private String memberEmail;
    private String memberName;
    private String bookTitle;
    private String reason;
}
