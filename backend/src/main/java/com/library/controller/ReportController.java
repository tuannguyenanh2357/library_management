package com.library.controller;

import com.library.dto.response.report.WeeklyRevenueReportResponse;
import com.library.service.interfaces.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /reports/revenue?from=2026-07-14&to=2026-07-21
     * Nếu không truyền tham số sẽ mặc định lấy 7 ngày gần nhất.
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<WeeklyRevenueReportResponse> getRevenueReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate toDate   = to   != null ? to   : LocalDate.now();
        LocalDate fromDate = from != null ? from : toDate.minusDays(6); // 7 ngày gần nhất

        return ResponseEntity.ok(reportService.getRevenueReport(fromDate, toDate));
    }
}
