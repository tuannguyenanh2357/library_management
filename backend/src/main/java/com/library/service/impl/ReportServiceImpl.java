package com.library.service.impl;

import com.library.dto.response.report.*;
import com.library.repository.ReportRepository;
import com.library.service.interfaces.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

        private final ReportRepository reportRepository;
        private final JasperReport revenueReportTemplate;
        private final JasperReport topOffendersSubReport;
        private final JasperReport topBooksSubReport;

        @Override
        public WeeklyRevenueReportResponse getRevenueReport(LocalDate fromDate, LocalDate toDate) {

                // 1. Tóm tắt
                List<ReportSummaryProjection> summaryList = reportRepository.getRevenueReportSummary(fromDate, toDate);

                WeeklyRevenueReportResponse.WeeklyRevenueReportResponseBuilder builder = WeeklyRevenueReportResponse
                                .builder()
                                .fromDate(fromDate)
                                .toDate(toDate);

                if (!summaryList.isEmpty()) {
                        ReportSummaryProjection s = summaryList.get(0);

                        BigDecimal collected = orZero(s.getCollectedAmount());
                        BigDecimal prevCollected = orZero(s.getPrevCollectedAmount());

                        double growth = 0.0;
                        if (prevCollected.compareTo(BigDecimal.ZERO) > 0) {
                                growth = collected.subtract(prevCollected)
                                                .divide(prevCollected, 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100))
                                                .doubleValue();
                        } else if (collected.compareTo(BigDecimal.ZERO) > 0) {
                                growth = 100.0;
                        }

                        builder
                                        .rangeDays(s.getRangeDays() != null ? s.getRangeDays() : 7)
                                        .collectedAmount(collected)
                                        .collectedCount(s.getCollectedCount() != null ? s.getCollectedCount() : 0)
                                        .pendingAmount(orZero(s.getPendingAmount()))
                                        .pendingCount(s.getPendingCount() != null ? s.getPendingCount() : 0)
                                        .totalBorrowings(s.getTotalBorrowings() != null ? s.getTotalBorrowings() : 0)
                                        .prevCollectedAmount(prevCollected)
                                        .growthPercent(Math.round(growth * 10.0) / 10.0);
                }

                // 2. Chi tiết theo ngày
                List<DailyBreakdownProjection> dailyList = reportRepository.getDailyBreakdown(fromDate, toDate);

                List<WeeklyRevenueReportResponse.DailyEntry> daily = dailyList.stream()
                                .map(d -> WeeklyRevenueReportResponse.DailyEntry.builder()
                                                .date(d.getReportDate())
                                                .dayName(translateDayName(d.getDayName()))
                                                .collectedAmount(orZero(d.getCollectedAmount()))
                                                .collectedCount(d.getCollectedCount() != null ? d.getCollectedCount()
                                                                : 0)
                                                .build())
                                .toList();

                builder.dailyBreakdown(daily);

                // 3. Người vi phạm nhiều nhất
                List<TopOffenderProjection> offenderList = reportRepository.getTopOffenders(fromDate, toDate);

                List<WeeklyRevenueReportResponse.TopOffenderEntry> offenders = offenderList.stream()
                                .map(o -> WeeklyRevenueReportResponse.TopOffenderEntry.builder()
                                                .memberId(o.getMemberId())
                                                .memberName(o.getMemberName())
                                                .email(o.getEmail())
                                                .fineCount(o.getFineCount() != null ? o.getFineCount() : 0)
                                                .totalFineAmount(orZero(o.getTotalFineAmount()))
                                                .paidAmount(orZero(o.getPaidAmount()))
                                                .unpaidAmount(orZero(o.getUnpaidAmount()))
                                                .build())
                                .toList();

                builder.topOffenders(offenders);

                // 4. Sách bị phạt nhiều nhất
                List<TopPenalizedBookProjection> bookList = reportRepository.getTopPenalizedBooks(fromDate, toDate);

                List<WeeklyRevenueReportResponse.TopPenalizedBookEntry> books = bookList.stream()
                                .map(b -> WeeklyRevenueReportResponse.TopPenalizedBookEntry.builder()
                                                .bookId(b.getBookId())
                                                .bookTitle(b.getBookTitle())
                                                .author(b.getAuthor())
                                                .fineCount(b.getFineCount() != null ? b.getFineCount() : 0)
                                                .totalFineAmount(orZero(b.getTotalFineAmount()))
                                                .build())
                                .toList();

                builder.topBooks(books);

                return builder.build();
        }

        private BigDecimal orZero(BigDecimal value) {
                return value != null ? value : BigDecimal.ZERO;
        }

        private String translateDayName(String englishName) {
                if (englishName == null)
                        return "";
                return switch (englishName.toLowerCase()) {
                        case "monday" -> "Thứ Hai";
                        case "tuesday" -> "Thứ Ba";
                        case "wednesday" -> "Thứ Tư";
                        case "thursday" -> "Thứ Năm";
                        case "friday" -> "Thứ Sáu";
                        case "saturday" -> "Thứ Bảy";
                        case "sunday" -> "Chủ Nhật";
                        default -> englishName;
                };
        }

        @Override
        public byte[] exportRevenuePdf(LocalDate fromDate, LocalDate toDate) {
                try {
                        // 1. Lấy dữ liệu
                        WeeklyRevenueReportResponse data = getRevenueReport(fromDate, toDate);

                        // 2. Chuẩn bị nguồn dữ liệu
                        JRBeanCollectionDataSource dailyDataSource = new JRBeanCollectionDataSource(
                                        data.getDailyBreakdown());
                        JRBeanCollectionDataSource topOffendersSource = new JRBeanCollectionDataSource(
                                        data.getTopOffenders());
                        JRBeanCollectionDataSource topBooksSource = new JRBeanCollectionDataSource(data.getTopBooks());

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                        // 3. Chuẩn bị tham số
                        Map<String, Object> parameters = new HashMap<>();
                        parameters.put("fromDate", fromDate.format(dtf));
                        parameters.put("toDate", toDate.format(dtf));
                        parameters.put("rangeDays", data.getRangeDays());
                        parameters.put("collectedAmount", data.getCollectedAmount());
                        parameters.put("collectedCount", data.getCollectedCount());
                        parameters.put("pendingAmount", data.getPendingAmount());
                        parameters.put("pendingCount", data.getPendingCount());
                        parameters.put("totalBorrowings", data.getTotalBorrowings());
                        parameters.put("prevCollectedAmount", data.getPrevCollectedAmount());
                        parameters.put("growthPercent", data.getGrowthPercent());
                        parameters.put("topOffendersData", topOffendersSource);
                        parameters.put("topBooksData", topBooksSource);
                        parameters.put("topOffendersSub", topOffendersSubReport);
                        parameters.put("topBooksSub", topBooksSubReport);

                        // 4. Điền báo cáo
                        JasperPrint jasperPrint = JasperFillManager.fillReport(revenueReportTemplate, parameters,
                                        dailyDataSource);

                        // 5. Xuất ra PDF
                        return JasperExportManager.exportReportToPdf(jasperPrint);

                } catch (JRException e) {
                        log.error("Error generating revenue report PDF", e);
                        throw new RuntimeException("Lỗi tạo file báo cáo PDF: " + e.getMessage());
                }
        }
}
