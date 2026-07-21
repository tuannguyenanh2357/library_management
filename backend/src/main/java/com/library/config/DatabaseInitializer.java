package com.library.config;

import com.library.entity.Member;
import com.library.entity.enums.MemberRole;
import com.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.jdbc.core.JdbcTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.setup.admin.username}")
    private String adminUsername;

    @Value("${app.setup.admin.password}")
    private String adminPassword;

    @Value("${app.setup.librarian.username}")
    private String librarianUsername;

    @Value("${app.setup.librarian.password}")
    private String librarianPassword;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initStoredProcedures();

        // Create Admin if not exists
        if (!memberRepository.existsByUsername(adminUsername)) {
            Member admin = Member.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .name("System Administrator")
                    .email("admin@library.com")
                    .phone("0999999999")
                    .address("Library HQ")
                    .role(MemberRole.ADMIN)
                    .isActive(true)
                    .build();
            memberRepository.save(admin);
            log.info("Default ADMIN account created: {} / {}", adminUsername, adminPassword);
        }

        // Create Librarian if not exists
        if (!memberRepository.existsByUsername(librarianUsername)) {
            Member librarian = Member.builder()
                    .username(librarianUsername)
                    .password(passwordEncoder.encode(librarianPassword))
                    .name("Main Librarian")
                    .email("librarian@library.com")
                    .phone("0888888888")
                    .address("Library Branch 1")
                    .role(MemberRole.LIBRARIAN)
                    .isActive(true)
                    .build();
            memberRepository.save(librarian);
            log.info("Default LIBRARIAN account created: {} / {}", librarianUsername, librarianPassword);
        }
    }

    private void initStoredProcedures() {
        try {
            jdbcTemplate.execute("IF OBJECT_ID('dbo.GetTop10MostBorrowedBooks', 'P') IS NOT NULL DROP PROCEDURE dbo.GetTop10MostBorrowedBooks;");

            jdbcTemplate.execute("""
                CREATE PROCEDURE dbo.GetTop10MostBorrowedBooks
                AS
                BEGIN
                    SET NOCOUNT ON;
                    
                    SELECT TOP 10
                        b.id AS BookId,
                        b.title AS Title,
                        b.author AS Author,
                        b.category AS Category,
                        b.image_url AS ImageUrl,
                        COUNT(br.id) AS BorrowCount
                    FROM dbo.books b
                    LEFT JOIN dbo.book_copies bc ON b.id = bc.book_id
                
                    LEFT JOIN dbo.borrowings br ON bc.id = br.book_copy_id AND br.borrow_date >= DATEADD(day, -7, GETDATE())
                    GROUP BY b.id, b.title, b.author, b.category, b.image_url
                    ORDER BY BorrowCount DESC;
                END;
            """);
            log.info("Stored Procedure dbo.GetTop10MostBorrowedBooks initialized successfully.");

            jdbcTemplate.execute("IF OBJECT_ID('dbo.GetMembersWithUnpaidFines', 'P') IS NOT NULL DROP PROCEDURE dbo.GetMembersWithUnpaidFines;");

            jdbcTemplate.execute("""
                CREATE PROCEDURE dbo.GetMembersWithUnpaidFines
                AS
                BEGIN
                    SET NOCOUNT ON;
                    
                    SELECT 
                        m.id AS MemberId,
                        m.member_code AS MemberCode,
                        m.name AS Name,
                        m.email AS Email,
                        m.phone AS Phone,
                        SUM(f.amount) AS TotalUnpaidAmount,
                        COUNT(f.id) AS UnpaidFinesCount
                    FROM dbo.members m
                    INNER JOIN dbo.borrowings br ON m.id = br.member_id
                    INNER JOIN dbo.fines f ON br.id = f.borrowing_id
                    WHERE f.status = 'UNPAID'
                    GROUP BY m.id, m.member_code, m.name, m.email, m.phone
                    ORDER BY TotalUnpaidAmount DESC;
                END;
            """);
            log.info("Stored Procedure dbo.GetMembersWithUnpaidFines initialized successfully.");

            jdbcTemplate.execute("IF OBJECT_ID('dbo.GetOverdueBooks', 'P') IS NOT NULL DROP PROCEDURE dbo.GetOverdueBooks;");

            jdbcTemplate.execute("""
                CREATE PROCEDURE dbo.GetOverdueBooks
                AS
                BEGIN
                    SET NOCOUNT ON;
                    
                    SELECT 
                        br.id AS BorrowingId,
                        m.id AS MemberId,
                        m.name AS MemberName,
                        m.phone AS MemberPhone,
                        b.title AS BookTitle,
                        bc.bar_code AS BarCode,
                        br.borrow_date AS BorrowDate,
                        br.due_date AS DueDate,
                        DATEDIFF(day, br.due_date, CAST(GETDATE() AS DATE)) AS OverdueDays
                    FROM dbo.borrowings br
                    INNER JOIN dbo.members m ON br.member_id = m.id
                    INNER JOIN dbo.book_copies bc ON br.book_copy_id = bc.id
                    INNER JOIN dbo.books b ON bc.book_id = b.id
                    WHERE br.status = 'ACTIVE' AND br.due_date < CAST(GETDATE() AS DATE)
                    ORDER BY OverdueDays DESC;
                END;
            """);
            log.info("Stored Procedure dbo.GetOverdueBooks initialized successfully.");

            // ---- Report SPs ----
            initReportSP("GetRevenueReportSummary", """
                CREATE PROCEDURE dbo.GetRevenueReportSummary
                    @FromDate DATE,
                    @ToDate   DATE
                AS
                BEGIN
                    SET NOCOUNT ON;
                    DECLARE @CurrentCollected  DECIMAL(18,2);
                    DECLARE @CurrentCount      INT;
                    DECLARE @CurrentPending    DECIMAL(18,2);
                    DECLARE @CurrentPendingCnt INT;
                    DECLARE @TotalBorrowings   INT;
                    SELECT @CurrentCollected = ISNULL(SUM(f.amount), 0), @CurrentCount = COUNT(f.id)
                    FROM dbo.fines f WHERE f.status = 'PAID' AND f.paid_date BETWEEN @FromDate AND @ToDate;
                    SELECT @CurrentPending = ISNULL(SUM(f.amount), 0), @CurrentPendingCnt = COUNT(f.id)
                    FROM dbo.fines f WHERE f.status = 'UNPAID' AND f.issued_date BETWEEN @FromDate AND @ToDate;
                    SELECT @TotalBorrowings = COUNT(id) FROM dbo.borrowings WHERE borrow_date BETWEEN @FromDate AND @ToDate;
                    DECLARE @PrevCollected DECIMAL(18,2);
                    DECLARE @RangeDays INT = DATEDIFF(day, @FromDate, @ToDate) + 1;
                    DECLARE @PrevFrom DATE = DATEADD(day, -@RangeDays, @FromDate);
                    DECLARE @PrevTo   DATE = DATEADD(day, -1, @FromDate);
                    SELECT @PrevCollected = ISNULL(SUM(f.amount), 0)
                    FROM dbo.fines f WHERE f.status = 'PAID' AND f.paid_date BETWEEN @PrevFrom AND @PrevTo;
                    SELECT @CurrentCollected AS CollectedAmount, @CurrentCount AS CollectedCount,
                           @CurrentPending AS PendingAmount, @CurrentPendingCnt AS PendingCount,
                           @TotalBorrowings AS TotalBorrowings, @PrevCollected AS PrevCollectedAmount,
                           @RangeDays AS RangeDays;
                END;
            """);

            initReportSP("GetRevenueDailyBreakdown", """
                CREATE PROCEDURE dbo.GetRevenueDailyBreakdown
                    @FromDate DATE,
                    @ToDate   DATE
                AS
                BEGIN
                    SET NOCOUNT ON;
                    WITH Dates AS (
                        SELECT @FromDate AS dt
                        UNION ALL
                        SELECT DATEADD(day, 1, dt) FROM Dates WHERE dt < @ToDate
                    )
                    SELECT d.dt AS ReportDate, DATENAME(weekday, d.dt) AS DayName,
                           ISNULL(SUM(f.amount), 0) AS CollectedAmount, ISNULL(COUNT(f.id), 0) AS CollectedCount
                    FROM Dates d
                    LEFT JOIN dbo.fines f ON f.paid_date = d.dt AND f.status = 'PAID'
                    GROUP BY d.dt ORDER BY d.dt OPTION (MAXRECURSION 365);
                END;
            """);

            initReportSP("GetTopOffendersInPeriod", """
                CREATE PROCEDURE dbo.GetTopOffendersInPeriod
                    @FromDate DATE,
                    @ToDate   DATE
                AS
                BEGIN
                    SET NOCOUNT ON;
                    SELECT TOP 5 m.id AS MemberId, m.name AS MemberName, m.email AS Email,
                           COUNT(f.id) AS FineCount, SUM(f.amount) AS TotalFineAmount,
                           SUM(CASE WHEN f.status = 'PAID' THEN f.amount ELSE 0 END) AS PaidAmount,
                           SUM(CASE WHEN f.status = 'UNPAID' THEN f.amount ELSE 0 END) AS UnpaidAmount
                    FROM dbo.fines f
                    INNER JOIN dbo.borrowings br ON f.borrowing_id = br.id
                    INNER JOIN dbo.members m ON br.member_id = m.id
                    WHERE f.issued_date BETWEEN @FromDate AND @ToDate
                    GROUP BY m.id, m.name, m.email ORDER BY TotalFineAmount DESC;
                END;
            """);

            initReportSP("GetTopPenalizedBooksInPeriod", """
                CREATE PROCEDURE dbo.GetTopPenalizedBooksInPeriod
                    @FromDate DATE,
                    @ToDate   DATE
                AS
                BEGIN
                    SET NOCOUNT ON;
                    SELECT TOP 5 b.id AS BookId, b.title AS BookTitle, b.author AS Author,
                           COUNT(f.id) AS FineCount, SUM(f.amount) AS TotalFineAmount
                    FROM dbo.fines f
                    INNER JOIN dbo.borrowings br ON f.borrowing_id = br.id
                    INNER JOIN dbo.book_copies bc ON br.book_copy_id = bc.id
                    INNER JOIN dbo.books b ON bc.book_id = b.id
                    WHERE f.issued_date BETWEEN @FromDate AND @ToDate
                    GROUP BY b.id, b.title, b.author ORDER BY FineCount DESC;
                END;
            """);

        } catch (Exception e) {
            log.error("Failed to initialize Stored Procedure: {}", e.getMessage(), e);
        }
    }

    private void initReportSP(String spName, String createSql) {
        try {
            jdbcTemplate.execute("IF OBJECT_ID('dbo." + spName + "', 'P') IS NOT NULL DROP PROCEDURE dbo." + spName + ";");
            jdbcTemplate.execute(createSql);
            log.info("Stored Procedure dbo.{} initialized successfully.", spName);
        } catch (Exception e) {
            log.error("Failed to initialize SP dbo.{}: {}", spName, e.getMessage());
        }
    }
}
