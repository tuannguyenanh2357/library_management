package com.library.dto.response;

import com.library.entity.enums.BookCopyStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class BookCopyResponse {
    Long id;
    String barCode;
    BookCopyStatus status;
    String author;
    String title;
    LocalDate dueDate;
    Long bookId;
}
