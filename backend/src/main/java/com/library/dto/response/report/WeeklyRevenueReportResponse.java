package com.library.dto.response.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class WeeklyRevenueReportResponse {

    // Thời gian báo cáo
    private LocalDate fromDate;
    private LocalDate toDate;
    private int       rangeDays;

    // Tổng quan kỳ này
    private BigDecimal collectedAmount;
    private int        collectedCount;
    private BigDecimal pendingAmount;
    private int        pendingCount;
    private int        totalBorrowings;

    // So sánh với kỳ trước
    private BigDecimal prevCollectedAmount;
    private Double     growthPercent;   // % tăng/giảm so với kỳ trước

    // Phân tích theo ngày
    private List<DailyEntry> dailyBreakdown;

    // Top vi phạm
    private List<TopOffenderEntry>      topOffenders;
    private List<TopPenalizedBookEntry> topBooks;

    // ---- Nested DTOs ----

    @Data @Builder
    public static class DailyEntry {
        private LocalDate  date;
        private String     dayName;
        private BigDecimal collectedAmount;
        private int        collectedCount;
    }

    @Data @Builder
    public static class TopOffenderEntry {
        private Long       memberId;
        private String     memberName;
        private String     email;
        private int        fineCount;
        private BigDecimal totalFineAmount;
        private BigDecimal paidAmount;
        private BigDecimal unpaidAmount;
    }

    @Data @Builder
    public static class TopPenalizedBookEntry {
        private Long       bookId;
        private String     bookTitle;
        private String     author;
        private int        fineCount;
        private BigDecimal totalFineAmount;
    }
}
