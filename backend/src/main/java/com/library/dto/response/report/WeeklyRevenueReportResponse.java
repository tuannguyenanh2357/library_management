package com.library.dto.response.report;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeeklyRevenueReportResponse {

    // Thời gian báo cáo
    LocalDate fromDate;
    LocalDate toDate;
    int rangeDays;

    // Tổng quan kỳ này
    BigDecimal collectedAmount;
    int collectedCount;
    BigDecimal pendingAmount;
    int pendingCount;
    int totalBorrowings;

    // So sánh với kỳ trước
    BigDecimal prevCollectedAmount;
    Double growthPercent;

    // Phân tích theo ngày
    List<DailyEntry> dailyBreakdown;

    // Top vi phạm
    List<TopOffenderEntry> topOffenders;
    List<TopPenalizedBookEntry> topBooks;

    // Lưu ý: các class dưới đây giữ getter kiểu JavaBean (getXxx) vì JasperReports
    // (JRBeanCollectionDataSource) đọc field bằng reflection theo chuẩn JavaBean.
    // Dùng record ở đây sẽ khiến PDF xuất ra bị trống các cột này.

    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DailyEntry {
        LocalDate date;
        String dayName;
        BigDecimal collectedAmount;
        int collectedCount;
    }

    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TopOffenderEntry {
        Long memberId;
        String memberName;
        String email;
        int fineCount;
        BigDecimal totalFineAmount;
        BigDecimal paidAmount;
        BigDecimal unpaidAmount;
    }

    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TopPenalizedBookEntry {
        Long bookId;
        String bookTitle;
        String author;
        int fineCount;
        BigDecimal totalFineAmount;
    }
}
