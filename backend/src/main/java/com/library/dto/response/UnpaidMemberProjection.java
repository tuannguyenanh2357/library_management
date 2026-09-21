package com.library.dto.response;

import java.math.BigDecimal;

public interface UnpaidMemberProjection {
    Long getMemberId();
    String getMemberCode();
    String getName();
    String getEmail();
    String getPhone();
    BigDecimal getTotalUnpaidAmount();
    Integer getUnpaidFinesCount();
}
