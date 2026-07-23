package com.library.dto.response.report;

import java.math.BigDecimal;

public interface TopPenalizedBookProjection {
    Long       getBookId();
    String     getBookTitle();
    String     getAuthor();
    Integer    getFineCount();
    BigDecimal getTotalFineAmount();
}
