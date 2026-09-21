package com.library.repository;

import com.library.dto.response.report.WeeklyRevenueReportResponse.TopOffenderEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class CustomFineRepositoryImpl implements CustomFineRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<TopOffenderEntry> getTopOffenders(LocalDate fromDate, LocalDate toDate, int limit) {
        String nativeSql = "SELECT m.id AS member_id, " +
                "       m.name AS member_name, " +
                "       m.email AS email, " +
                "       COUNT(f.id) AS fine_count, " +
                "       SUM(f.amount) AS total_fine_amount, " +
                "       SUM(CASE WHEN f.status = 'PAID' THEN f.amount ELSE 0 END) AS paid_amount, " +
                "       SUM(CASE WHEN f.status = 'UNPAID' THEN f.amount ELSE 0 END) AS unpaid_amount " +
                "FROM fines f " +
                "JOIN borrowing b ON f.borrowing_id = b.id " +
                "JOIN members m ON b.member_id = m.id " +
                "WHERE f.issued_date >= :fromDate AND f.issued_date <= :toDate " +
                "GROUP BY m.id, m.name, m.email " +
                "ORDER BY total_fine_amount DESC " +
                "LIMIT :limit";

        Query query = entityManager.createNativeQuery(nativeSql, "TopOffendersMapping");
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("limit", limit);

        return query.getResultList();
    }
}
