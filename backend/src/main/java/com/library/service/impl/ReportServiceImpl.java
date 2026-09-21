package com.library.service.impl;

import com.library.dto.response.report.*;
import com.library.repository.ReportRepository;
import com.library.repository.FineRepository;
import com.library.service.interfaces.ReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.NonFinal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportServiceImpl implements ReportService {

        @NonFinal
        @Value("${app.report.top-n:5}")
        int topN;

        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        ReportRepository reportRepository;
        FineRepository fineRepository; // Thêm FineRepository vào đây
        JasperReport revenueReportTemplate;
        JasperReport topOffendersSubReport;
        JasperReport topBooksSubReport;

        // Lấy toàn bộ dữ liệu báo cáo doanh thu tổng hợp trong một khoảng thời gian.
        @Override
        public WeeklyRevenueReportResponse getRevenueReport(LocalDate fromDate, LocalDate toDate) {
                var builder = WeeklyRevenueReportResponse.builder()
                                .fromDate(fromDate)
                                .toDate(toDate)
                                .dailyBreakdown(buildDailyBreakdown(fromDate, toDate)) // lấy doanh thu theo ngày
                                .topOffenders(buildTopOffenders(fromDate, toDate)) // top độc giả vi phạm
                                .topBooks(buildTopBooks(fromDate, toDate)); // top sách vi phạm

                reportRepository.getRevenueReportSummary(fromDate, toDate).stream()
                                .findFirst() // lấy tổng số liệu
                                .ifPresent(s -> {
                                        BigDecimal collected = orZero(s.getCollectedAmount());
                                        BigDecimal prev = orZero(s.getPrevCollectedAmount());

                                        builder.rangeDays(orDefault(s.getRangeDays(), 7))
                                                        .collectedAmount(collected)
                                                        .collectedCount(orZero(s.getCollectedCount()))
                                                        .pendingAmount(orZero(s.getPendingAmount()))
                                                        .pendingCount(orZero(s.getPendingCount()))
                                                        .totalBorrowings(orZero(s.getTotalBorrowings()))
                                                        .prevCollectedAmount(prev)
                                                        .growthPercent(calculateGrowthPercent(collected, prev));
                                });

                return builder.build();
        }

        // Tính toán phần trăm tăng trưởng doanh thu so với kỳ trước đó.
        private double calculateGrowthPercent(BigDecimal collected, BigDecimal prevCollected) {
                if (prevCollected.signum() == 0) {
                        return collected.signum() > 0 ? 100.0 : 0.0;
                }
                double c = collected.doubleValue();
                double p = prevCollected.doubleValue();
                return Math.round((c - p) / p * 1000.0) / 10.0; // Làm tròn 1 chữ số thập phân
        }

        // danh sách thống kê doanh thu theo từng ngày.
        private List<WeeklyRevenueReportResponse.DailyEntry> buildDailyBreakdown(LocalDate fromDate, LocalDate toDate) {
                return reportRepository.getDailyBreakdown(fromDate, toDate).stream()
                                .map(d -> new WeeklyRevenueReportResponse.DailyEntry(
                                                d.getReportDate(),
                                                translateDayName(d.getDayName()),
                                                orZero(d.getCollectedAmount()),
                                                orZero(d.getCollectedCount())))
                                .toList();
        }

        // danh sách top những độc giả bị phạt nhiều nhất
        private List<WeeklyRevenueReportResponse.TopOffenderEntry> buildTopOffenders(LocalDate fromDate,
                        LocalDate toDate) {
                // Sử dụng EntityManager + @SqlResultSetMapping thông qua FineRepository
                return fineRepository.getTopOffenders(fromDate, toDate, topN);
        }

        // danh sách top những cuốn sách bị phạt nhiều nhất
        private List<WeeklyRevenueReportResponse.TopPenalizedBookEntry> buildTopBooks(LocalDate fromDate,
                        LocalDate toDate) {
                return reportRepository.getTopPenalizedBooks(fromDate, toDate, PageRequest.of(0, topN)).stream()
                                .map(b -> new WeeklyRevenueReportResponse.TopPenalizedBookEntry(
                                                b.getBookId(),
                                                b.getBookTitle(),
                                                b.getAuthor(),
                                                orZero(b.getFineCount()),
                                                orZero(b.getTotalFineAmount())))
                                .toList();
        }

        // Hàm tiện ích (Utility): Xử lý an toàn giá trị null đối với BigDecimal, trả về
        // 0 nếu bị null.
        private BigDecimal orZero(BigDecimal value) {
                return value != null ? value : BigDecimal.ZERO;
        }

        private int orZero(Integer value) {
                return value != null ? value : 0;
        }

        private int orDefault(Integer value, int defaultValue) {
                return value != null ? value : defaultValue;
        }

        private String translateDayName(String englishName) {
                if (englishName == null) {
                        return "";
                }
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

        // Hàm phục vụ việc kết xuất (Export) toàn bộ dữ liệu báo cáo ra file định dạng
        // PDF sử dụng thư viện JasperReports. Trả về mảng byte (byte array) của file
        // PDF.
        @Override
        public byte[] exportRevenuePdf(LocalDate fromDate, LocalDate toDate) {
                WeeklyRevenueReportResponse data = getRevenueReport(fromDate, toDate);

                try {
                        JasperPrint jasperPrint = JasperFillManager.fillReport(
                                        revenueReportTemplate,
                                        buildPdfParameters(fromDate, toDate, data),
                                        new JRBeanCollectionDataSource(data.getDailyBreakdown()));

                        return JasperExportManager.exportReportToPdf(jasperPrint);
                } catch (JRException e) {
                        log.error("Error generating revenue report PDF", e);
                        throw new RuntimeException("Lỗi tạo file báo cáo PDF: " + e.getMessage(), e);
                }
        }

        // Đóng gói tất cả các thông số (parameters) cần thiết vào một Map để truyền
        // trực tiếp vào các placeholder/field trong template của JasperReports.
        private Map<String, Object> buildPdfParameters(LocalDate fromDate, LocalDate toDate,
                        WeeklyRevenueReportResponse data) {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("fromDate", fromDate.format(DATE_FORMAT));
                parameters.put("toDate", toDate.format(DATE_FORMAT));
                parameters.put("rangeDays", data.getRangeDays());
                parameters.put("collectedAmount", data.getCollectedAmount());
                parameters.put("collectedCount", data.getCollectedCount());
                parameters.put("pendingAmount", data.getPendingAmount());
                parameters.put("pendingCount", data.getPendingCount());
                parameters.put("totalBorrowings", data.getTotalBorrowings());
                parameters.put("prevCollectedAmount", data.getPrevCollectedAmount());
                parameters.put("growthPercent", data.getGrowthPercent());
                parameters.put("topOffendersData", new JRBeanCollectionDataSource(data.getTopOffenders()));
                parameters.put("topBooksData", new JRBeanCollectionDataSource(data.getTopBooks()));
                parameters.put("topOffendersSub", topOffendersSubReport);
                parameters.put("topBooksSub", topBooksSubReport);
                return parameters;
        }
}
