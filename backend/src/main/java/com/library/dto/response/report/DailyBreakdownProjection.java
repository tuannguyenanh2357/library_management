package com.library.dto.response.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyBreakdownProjection {
    LocalDate  getReportDate();
    String     getDayName();
    BigDecimal getCollectedAmount();
    Integer    getCollectedCount();
}
