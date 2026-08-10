package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class BorrowingResponse {
    Long id;
    Long memberId;
    String memberName;
    Long bookId;
    String barCode;
    String bookTitle;
    String borrowDate;
    String dueDate;
    String returnDate;
    String bookCopy;
}
