package com.library.repository;

import com.library.dto.response.report.WeeklyRevenueReportResponse.TopOffenderEntry;
import java.time.LocalDate;
import java.util.List;

public interface CustomFineRepository {
    List<TopOffenderEntry> getTopOffenders(LocalDate fromDate, LocalDate toDate, int limit);
}
