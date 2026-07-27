-- top 10 sách được mượn nhiều nhất
IF OBJECT_ID('dbo.GetTop10MostBorrowedBooks', 'P') IS NOT NULL
    DROP PROCEDURE dbo.GetTop10MostBorrowedBooks;
GO

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
    -- LEFT JOIN dbo.borrowings br ON bc.id = br.book_copy_id
    LEFT JOIN dbo.borrowings br ON bc.id = br.book_copy_id AND br.borrow_date >= DATEADD(day, -7, GETDATE())
    GROUP BY b.id, b.title, b.author, b.category, b.image_url
    ORDER BY BorrowCount DESC;
END;
GO


-- Thành viên nợ phạt quá hạn
IF OBJECT_ID('dbo.GetMembersWithUnpaidFines', 'P') IS NOT NULL
    DROP PROCEDURE dbo.GetMembersWithUnpaidFines;
GO

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
GO


-- Sách quá hạn chưa trả
IF OBJECT_ID('dbo.GetOverdueBooks', 'P') IS NOT NULL
    DROP PROCEDURE dbo.GetOverdueBooks;
GO

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
GO

-- Báo cáo doanh thu phạt theo khoảng thời gian


-- 1. Thống kê tổng quan
IF OBJECT_ID('dbo.GetRevenueReportSummary', 'P') IS NOT NULL
    DROP PROCEDURE dbo.GetRevenueReportSummary;
GO

CREATE PROCEDURE dbo.GetRevenueReportSummary
    @FromDate DATE,
    @ToDate   DATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Kỳ hiện tại
    DECLARE @CurrentCollected  DECIMAL(18,2);
    DECLARE @CurrentCount      INT;
    DECLARE @CurrentPending    DECIMAL(18,2);
    DECLARE @CurrentPendingCnt INT;
    DECLARE @TotalBorrowings   INT;

    SELECT
        @CurrentCollected = ISNULL(SUM(f.amount), 0),
        @CurrentCount     = COUNT(f.id)
    FROM dbo.fines f
    WHERE f.status = 'PAID'
      AND f.paid_date BETWEEN @FromDate AND @ToDate;

    SELECT
        @CurrentPending    = ISNULL(SUM(f.amount), 0),
        @CurrentPendingCnt = COUNT(f.id)
    FROM dbo.fines f
    WHERE f.status = 'UNPAID'
      AND f.issued_date BETWEEN @FromDate AND @ToDate;

    SELECT @TotalBorrowings = COUNT(id)
    FROM dbo.borrowings
    WHERE borrow_date BETWEEN @FromDate AND @ToDate;

    -- Kỳ trước (cùng độ dài)
    DECLARE @PrevCollected DECIMAL(18,2);
    DECLARE @RangeDays     INT = DATEDIFF(day, @FromDate, @ToDate) + 1;
    DECLARE @PrevFrom DATE = DATEADD(day, -@RangeDays, @FromDate);
    DECLARE @PrevTo   DATE = DATEADD(day, -1, @FromDate);

    SELECT @PrevCollected = ISNULL(SUM(f.amount), 0)
    FROM dbo.fines f
    WHERE f.status = 'PAID'
      AND f.paid_date BETWEEN @PrevFrom AND @PrevTo;

    SELECT
        @CurrentCollected  AS CollectedAmount,
        @CurrentCount      AS CollectedCount,
        @CurrentPending    AS PendingAmount,
        @CurrentPendingCnt AS PendingCount,
        @TotalBorrowings   AS TotalBorrowings,
        @PrevCollected     AS PrevCollectedAmount,
        @RangeDays         AS RangeDays;
END;
GO


-- 2. Phân tích theo ngày
IF OBJECT_ID('dbo.GetRevenueDailyBreakdown', 'P') IS NOT NULL
    DROP PROCEDURE dbo.GetRevenueDailyBreakdown;
GO

CREATE PROCEDURE dbo.GetRevenueDailyBreakdown
    @FromDate DATE,
    @ToDate   DATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Generate date series using a numbers table trick
    WITH Dates AS (
        SELECT @FromDate AS dt
        UNION ALL
        SELECT DATEADD(day, 1, dt)
        FROM Dates
        WHERE dt < @ToDate
    )
    SELECT
        d.dt                                  AS ReportDate,
        DATENAME(weekday, d.dt)               AS DayName,
        ISNULL(SUM(f.amount), 0)              AS CollectedAmount,
        ISNULL(COUNT(f.id), 0)                AS CollectedCount
    FROM Dates d
    LEFT JOIN dbo.fines f
        ON f.paid_date = d.dt AND f.status = 'PAID'
    GROUP BY d.dt
    ORDER BY d.dt
    OPTION (MAXRECURSION 365);
END;
GO


-- 3. Top 5 độc giả vi phạm nhiều nhất trong kỳ
IF OBJECT_ID('dbo.GetTopOffendersInPeriod', 'P') IS NOT NULL
    DROP PROCEDURE dbo.GetTopOffendersInPeriod;
GO

CREATE PROCEDURE dbo.GetTopOffendersInPeriod
    @FromDate DATE,
    @ToDate   DATE
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP 5
        m.id                    AS MemberId,
        m.name                  AS MemberName,
        m.email                 AS Email,
        COUNT(f.id)             AS FineCount,
        SUM(f.amount)           AS TotalFineAmount,
        SUM(CASE WHEN f.status = 'PAID' THEN f.amount ELSE 0 END)   AS PaidAmount,
        SUM(CASE WHEN f.status = 'UNPAID' THEN f.amount ELSE 0 END) AS UnpaidAmount
    FROM dbo.fines f
    INNER JOIN dbo.borrowings br ON f.borrowing_id = br.id
    INNER JOIN dbo.members m     ON br.member_id = m.id
    WHERE (f.status = 'PAID' AND f.paid_date BETWEEN @FromDate AND @ToDate)
       OR (f.status = 'UNPAID' AND f.issued_date BETWEEN @FromDate AND @ToDate)
    GROUP BY m.id, m.name, m.email
    ORDER BY TotalFineAmount DESC;
END;
GO


-- 4. Top 5 sách bị phạt nhiều nhất trong kỳ
IF OBJECT_ID('dbo.GetTopPenalizedBooksInPeriod', 'P') IS NOT NULL
    DROP PROCEDURE dbo.GetTopPenalizedBooksInPeriod;
GO

CREATE PROCEDURE dbo.GetTopPenalizedBooksInPeriod
    @FromDate DATE,
    @ToDate   DATE
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP 5
        b.id                    AS BookId,
        b.title                 AS BookTitle,
        b.author                AS Author,
        COUNT(f.id)             AS FineCount,
        SUM(f.amount)           AS TotalFineAmount
    FROM dbo.fines f
    INNER JOIN dbo.borrowings br ON f.borrowing_id = br.id
    INNER JOIN dbo.book_copies bc ON br.book_copy_id = bc.id
    INNER JOIN dbo.books b        ON bc.book_id = b.id
    WHERE (f.status = 'PAID' AND f.paid_date BETWEEN @FromDate AND @ToDate)
       OR (f.status = 'UNPAID' AND f.issued_date BETWEEN @FromDate AND @ToDate)
    GROUP BY b.id, b.title, b.author
    ORDER BY FineCount DESC;
END;
GO
