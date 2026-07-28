package com.library.dto.response;

import java.time.LocalDate;

public interface OverdueBookProjection {
    Long getBorrowingId();
    Long getMemberId();
    String getMemberName();
    String getMemberPhone();
    String getBookTitle();
    String getBarCode();
    LocalDate getBorrowDate();
    LocalDate getDueDate();
    Integer getOverdueDays();
}
