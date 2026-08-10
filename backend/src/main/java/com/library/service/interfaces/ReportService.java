package com.library.service.interfaces;

import com.library.dto.response.report.WeeklyRevenueReportResponse;

import java.time.LocalDate;

public interface ReportService {
    // lấy báo cáo doanh thu theo khoảng thời gian
    WeeklyRevenueReportResponse getRevenueReport(LocalDate fromDate, LocalDate toDate);

    // xuất báo cáo doanh thu theo khoảng thời gian dưới dạng file PDF
    byte[] exportRevenuePdf(LocalDate fromDate, LocalDate toDate);
}
