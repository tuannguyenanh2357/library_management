package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingRequestResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long bookId;
    private String bookTitle;
    private String status;
    private LocalDateTime requestDate;
    private LocalDate expectedDueDate;
    private LocalDateTime processedDate;
    private String notes;
    private long availableCopiesCount;
    // ID của BookCopy đang được giữ chỗ (chỉ có giá trị khi status = APPROVED)
    private Long assignedBookCopyId;
    // Thời điểm được duyệt (dùng để hiển thị thời gian đã chờ)
    private LocalDateTime approvedDate;
}

