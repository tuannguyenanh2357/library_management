package com.library.service.interfaces;

import com.library.dto.response.report.WeeklyRevenueReportResponse;

import java.time.LocalDate;

public interface ReportService {
    WeeklyRevenueReportResponse getRevenueReport(LocalDate fromDate, LocalDate toDate);
}
