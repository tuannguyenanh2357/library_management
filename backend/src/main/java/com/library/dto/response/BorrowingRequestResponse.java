package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BorrowingRequestResponse {
    Long id;
    Long memberId;
    String memberName;
    Long bookId;
    String bookTitle;
    String status;
    LocalDateTime requestDate;
    LocalDate expectedDueDate;
    LocalDateTime processedDate;
    String notes;
    long availableCopiesCount;
    // ID của BookCopy đang được giữ chỗ (chỉ có giá trị khi status = APPROVED)
    Long assignedBookCopyId;
    // Thời điểm được duyệt (dùng để hiển thị thời gian đã chờ)
    LocalDateTime approvedDate;
}
