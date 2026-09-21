package com.library.dto.response.report;

import java.math.BigDecimal;

public interface TopOffenderProjection {
    Long       getMemberId();
    String     getMemberName();
    String     getEmail();
    Integer    getFineCount();
    BigDecimal getTotalFineAmount();
    BigDecimal getPaidAmount();
    BigDecimal getUnpaidAmount();
}
