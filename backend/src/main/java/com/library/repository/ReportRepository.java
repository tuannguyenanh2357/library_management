package com.library.repository;

import com.library.dto.response.report.DailyBreakdownProjection;
import com.library.dto.response.report.ReportSummaryProjection;
import com.library.dto.response.report.TopOffenderProjection;
import com.library.dto.response.report.TopPenalizedBookProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.entity.Fines;

import java.time.LocalDate;
import java.util.List;

public interface ReportRepository extends JpaRepository<Fines, Long> {

       // Gọi Stored Procedure để lấy tóm tắt tổng quan báo cáo doanh thu phạt trong một khoảng thời gian
       @Query(value = "EXEC dbo.GetRevenueReportSummary @FromDate = :fromDate, @ToDate = :toDate", nativeQuery = true)
       List<ReportSummaryProjection> getRevenueReportSummary(
                     @Param("fromDate") LocalDate fromDate,
                     @Param("toDate") LocalDate toDate);

       // Gọi Stored Procedure để lấy thống kê doanh thu phạt chi tiết theo từng ngày
       @Query(value = "EXEC dbo.GetRevenueDailyBreakdown @FromDate = :fromDate, @ToDate = :toDate", nativeQuery = true)
       List<DailyBreakdownProjection> getDailyBreakdown(
                     @Param("fromDate") LocalDate fromDate,
                     @Param("toDate") LocalDate toDate);

       // Gọi Stored Procedure để lấy danh sách những thành viên vi phạm (bị phạt) nhiều nhất
       @Query(value = "EXEC dbo.GetTopOffendersInPeriod @FromDate = :fromDate, @ToDate = :toDate", nativeQuery = true)
       List<TopOffenderProjection> getTopOffenders(
                     @Param("fromDate") LocalDate fromDate,
                     @Param("toDate") LocalDate toDate);

       // Gọi Stored Procedure để lấy danh sách những đầu sách bị phạt nhiều nhất
       @Query(value = "EXEC dbo.GetTopPenalizedBooksInPeriod @FromDate = :fromDate, @ToDate = :toDate", nativeQuery = true)
       List<TopPenalizedBookProjection> getTopPenalizedBooks(
                     @Param("fromDate") LocalDate fromDate,
                     @Param("toDate") LocalDate toDate);
}
