package com.library.dto.response;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public interface OverdueBookProjection {
    Long getBorrowingId();
    Long getMemberId();
    String getMemberName();
    String getMemberPhone();
    String getBookTitle();
    String getBarCode();
    LocalDate getBorrowDate();
    LocalDate getDueDate();

    default Integer getOverdueDays() {
        return (int) ChronoUnit.DAYS.between(getDueDate(), LocalDate.now());
    }
}
