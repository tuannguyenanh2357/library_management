package com.library.dto.response;

import java.time.LocalDate;

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
    LocalDate borrowDate;
    LocalDate dueDate;
    LocalDate returnDate;
    Long bookCopy;
    Integer renewalCount;

}
