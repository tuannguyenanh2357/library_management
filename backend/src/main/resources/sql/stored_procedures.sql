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
        COUNT(DISTINCT br.id) AS BorrowCount
    FROM dbo.books b
    LEFT JOIN dbo.book_copies bc ON b.id = bc.book_id
    LEFT JOIN dbo.borrowings br ON bc.id = br.book_copy_id AND br.borrow_date >= DATEADD(day, -30, GETDATE())
    GROUP BY b.id, b.title, b.author, b.category, b.image_url
    ORDER BY BorrowCount DESC, b.id DESC;
END;
GO


-- . Thống kê tổng quan
IF OBJECT_ID('dbo.GetRevenueReportSummary', 'P') IS NOT NULL
    DROP PROCEDURE dbo.GetRevenueReportSummary;
GO

CREATE PROCEDURE dbo.GetRevenueReportSummary
    @FromDate DATE,
    @ToDate   DATE
AS
BEGIN
    SET NOCOUNT ON;

    -- ký hiện tại
    DECLARE @CurrentCollected  DECIMAL(18,2); -- tiền phạt đã thu
    DECLARE @CurrentCount      INT; -- số lần nộp tiền đã thu
    DECLARE @CurrentPending    DECIMAL(18,2); -- tiền phạt chưa thu
    DECLARE @CurrentPendingCnt INT; -- số khoản phạt chưa thanh toán
    DECLARE @TotalBorrowings   INT; -- tổng số lượt mượn

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

    -- Kỳ trước 
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
        @PrevCollected     AS PrevCollectedAmount, -- tiền phạt đã thu kỳ trước 
        @RangeDays         AS RangeDays;  -- khoảng thời gian bao nhiêu ngày
END;
GO


--  Phân tích theo ngày
IF OBJECT_ID('dbo.GetRevenueDailyBreakdown', 'P') IS NOT NULL
    DROP PROCEDURE dbo.GetRevenueDailyBreakdown;
GO

CREATE PROCEDURE dbo.GetRevenueDailyBreakdown
    @FromDate DATE,
    @ToDate   DATE
AS
BEGIN
    SET NOCOUNT ON;

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
    OPTION (MAXRECURSION 365); -- set tối đa 365 ngày
END;
GO

