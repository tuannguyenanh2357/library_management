package com.library.repository;

import com.library.dto.response.report.DailyBreakdownProjection;
import com.library.dto.response.report.ReportSummaryProjection;
import com.library.dto.response.report.TopOffenderProjection;
import com.library.dto.response.report.TopPenalizedBookProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.library.entity.Fines;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Fines, Long> {

    @Query(value = "EXEC dbo.GetRevenueReportSummary @FromDate = :fromDate, @ToDate = :toDate",
           nativeQuery = true)
    List<ReportSummaryProjection> getRevenueReportSummary(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate")   LocalDate toDate);

    @Query(value = "EXEC dbo.GetRevenueDailyBreakdown @FromDate = :fromDate, @ToDate = :toDate",
           nativeQuery = true)
    List<DailyBreakdownProjection> getDailyBreakdown(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate")   LocalDate toDate);

    @Query(value = "EXEC dbo.GetTopOffendersInPeriod @FromDate = :fromDate, @ToDate = :toDate",
           nativeQuery = true)
    List<TopOffenderProjection> getTopOffenders(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate")   LocalDate toDate);

    @Query(value = "EXEC dbo.GetTopPenalizedBooksInPeriod @FromDate = :fromDate, @ToDate = :toDate",
           nativeQuery = true)
    List<TopPenalizedBookProjection> getTopPenalizedBooks(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate")   LocalDate toDate);
}
