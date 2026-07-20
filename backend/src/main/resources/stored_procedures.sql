USE library_management;
GO  

IF OBJECT_ID('dbo.CalculateFine', 'P') IS NOT NULL
    DROP PROCEDURE dbo.CalculateFine;
GO

-- kiểm tra tính tiền phạt
CREATE PROCEDURE dbo.CalculateFine
    @BorrowingId INT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @DueDate DATE;
    DECLARE @ReturnDate DATE;
    DECLARE @OverdueDays INT;
    DECLARE @Amount DECIMAL(10, 2);
    
    SELECT @DueDate = due_date, @ReturnDate = return_date
    FROM dbo.borrowings
    WHERE id = @BorrowingId;
    
    IF @DueDate IS NOT NULL
    BEGIN
        IF @ReturnDate IS NOT NULL
            SET @OverdueDays = DATEDIFF(day, @DueDate, @ReturnDate);
        ELSE
            SET @OverdueDays = DATEDIFF(day, @DueDate, CAST(GETDATE() AS DATE));
            
        IF @OverdueDays > 0
        BEGIN
            SET @Amount = @OverdueDays * 5000.00;
            
            IF EXISTS (SELECT 1 FROM dbo.fines WHERE borrowing_id = @BorrowingId)
            BEGIN
                UPDATE dbo.fines
                SET amount = @Amount,
                    reason = N'Quá hạn trả sách ' + CAST(@OverdueDays AS NVARCHAR(10)) + N' ngày'
                WHERE borrowing_id = @BorrowingId AND status = 'UNPAID';
            END
            ELSE
            BEGIN
                INSERT INTO dbo.fines (borrowing_id, amount, reason, status, issued_date)
                VALUES (@BorrowingId, @Amount, N'Quá hạn trả sách ' + CAST(@OverdueDays AS NVARCHAR(10)) + N' ngày', 'UNPAID', CAST(GETDATE() AS DATE));
            END
        END
    END
END;
GO  

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
        COUNT(br.id) AS BorrowCount
    FROM dbo.books b
    LEFT JOIN dbo.book_copies bc ON b.id = bc.book_id
    LEFT JOIN dbo.borrowings br ON bc.id = br.book_copy_id
    GROUP BY b.id, b.title, b.author, b.category
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


-- Sách sắp hết hạn mượn trong số ngày tới
IF OBJECT_ID('dbo.GetBooksExpiringInNextDays', 'P') IS NOT NULL
    DROP PROCEDURE dbo.GetBooksExpiringInNextDays;
GO

CREATE PROCEDURE dbo.GetBooksExpiringInNextDays
    @Days INT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        br.id AS BorrowingId,
        m.name AS MemberName,
        b.title AS BookTitle,
        bc.bar_code AS BarCode,
        br.borrow_date AS BorrowDate,
        br.due_date AS DueDate,
        DATEDIFF(day, CAST(GETDATE() AS DATE), br.due_date) AS DaysRemaining
    FROM dbo.borrowings br
    INNER JOIN dbo.members m ON br.member_id = m.id
    INNER JOIN dbo.book_copies bc ON br.book_copy_id = bc.id
    INNER JOIN dbo.books b ON bc.book_id = b.id
    WHERE br.status = 'ACTIVE' 
      AND br.due_date >= CAST(GETDATE() AS DATE)
      AND br.due_date <= DATEADD(day, @Days, CAST(GETDATE() AS DATE))
    ORDER BY br.due_date ASC;
END;
GO


-- Doanh thu phạt theo tháng
IF OBJECT_ID('dbo.GetMonthlyFineRevenue', 'P') IS NOT NULL
    DROP PROCEDURE dbo.GetMonthlyFineRevenue;
GO

CREATE PROCEDURE dbo.GetMonthlyFineRevenue
    @Year INT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        MONTH(f.paid_date) AS [Month],
        SUM(f.amount) AS TotalRevenue,
        COUNT(f.id) AS TotalPaidFines
    FROM dbo.fines f
    WHERE f.status = 'PAID' AND YEAR(f.paid_date) = @Year
    GROUP BY MONTH(f.paid_date)
    ORDER BY [Month] ASC;
END;
GO
