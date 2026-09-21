package com.library.dto.response.report;

import java.math.BigDecimal;

public interface ReportSummaryProjection {
    BigDecimal getCollectedAmount();
    Integer    getCollectedCount();
    BigDecimal getPendingAmount();
    Integer    getPendingCount();
    Integer    getTotalBorrowings();
    BigDecimal getPrevCollectedAmount();
    Integer    getRangeDays();
}
