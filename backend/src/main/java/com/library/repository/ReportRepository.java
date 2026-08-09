package com.library.repository;

import com.library.dto.response.report.DailyBreakdownProjection;
import com.library.dto.response.report.ReportSummaryProjection;
import com.library.dto.response.report.TopOffenderProjection;
import com.library.dto.response.report.TopPenalizedBookProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.library.entity.Fines;
import com.library.entity.enums.FineStatus;

import java.time.LocalDate;
import java.util.List;

public interface ReportRepository extends JpaRepository<Fines, Long> {

       // Gọi SP để lấy tóm tắt tổng quan báo cáo doanh thu phạt trong một khoảng thời
       // gian
       @Query(value = "EXEC dbo.GetRevenueReportSummary @FromDate = :fromDate, @ToDate = :toDate", nativeQuery = true)
       List<ReportSummaryProjection> getRevenueReportSummary(
                     @Param("fromDate") LocalDate fromDate,
                     @Param("toDate") LocalDate toDate);

       // Gọi SP để lấy thống kê doanh thu phạt chi tiết theo từng ngày
       @Query(value = "EXEC dbo.GetRevenueDailyBreakdown @FromDate = :fromDate, @ToDate = :toDate", nativeQuery = true)
       List<DailyBreakdownProjection> getDailyBreakdown(
                     @Param("fromDate") LocalDate fromDate,
                     @Param("toDate") LocalDate toDate);

       // Lấy danh sách những thành viên vi phạm nhiều nhất trong kỳ
       @Query("SELECT m.id AS memberId, m.name AS memberName, m.email AS email, " +
                     "COUNT(f.id) AS fineCount, SUM(f.amount) AS totalFineAmount, " +
                     "SUM(CASE WHEN f.status = FineStatus.PAID THEN f.amount ELSE 0 END) AS paidAmount, "
                     +
                     "SUM(CASE WHEN f.status = FineStatus.UNPAID THEN f.amount ELSE 0 END) AS unpaidAmount "
                     +
                     "FROM Fines f " +
                     "JOIN f.borrowing br " +
                     "JOIN br.member m " +
                     "WHERE (f.status = FineStatus.PAID AND f.paidDate BETWEEN :fromDate AND :toDate) "
                     +
                     "   OR (f.status = FineStatus.UNPAID AND f.issuedDate BETWEEN :fromDate AND :toDate) "
                     +
                     "GROUP BY m.id, m.name, m.email " +
                     "ORDER BY SUM(f.amount) DESC")
       List<TopOffenderProjection> getTopOffenders(
                     @Param("fromDate") LocalDate fromDate,
                     @Param("toDate") LocalDate toDate,
                     Pageable pageable);

       // Lấy danh sách những đầu sách bị phạt nhiều nhất trong kỳ
       @Query("SELECT bk.id AS bookId, bk.title AS bookTitle, bk.author AS author, " +
                     "COUNT(f.id) AS fineCount, SUM(f.amount) AS totalFineAmount " +
                     "FROM Fines f " +
                     "JOIN f.borrowing br " +
                     "JOIN br.bookCopy bc " +
                     "JOIN bc.book bk " +
                     "WHERE (f.status = FineStatus.PAID AND f.paidDate BETWEEN :fromDate AND :toDate) "
                     +
                     "   OR (f.status = FineStatus.UNPAID AND f.issuedDate BETWEEN :fromDate AND :toDate) "
                     +
                     "GROUP BY bk.id, bk.title, bk.author " +
                     "ORDER BY COUNT(f.id) DESC")
       List<TopPenalizedBookProjection> getTopPenalizedBooks(
                     @Param("fromDate") LocalDate fromDate,
                     @Param("toDate") LocalDate toDate,
                     Pageable pageable);
}
