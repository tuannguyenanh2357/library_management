-- Script to convert book text columns to NVARCHAR to preserve Unicode (Vietnamese)
-- Backup your database before running these statements.
-- Adjust NULL/NOT NULL and lengths as needed.

USE LibraryManagement;
GO

ALTER TABLE dbo.books ALTER COLUMN title NVARCHAR(255) NULL;
ALTER TABLE dbo.books ALTER COLUMN author NVARCHAR(255) NULL;
ALTER TABLE dbo.books ALTER COLUMN publisher NVARCHAR(255) NULL;
ALTER TABLE dbo.books ALTER COLUMN isbn NVARCHAR(100) NULL;
ALTER TABLE dbo.books ALTER COLUMN category NVARCHAR(255) NULL;
ALTER TABLE dbo.books ALTER COLUMN description NVARCHAR(MAX) NULL;
GO

-- If some columns are NOT NULL in your schema, set the appropriate NOT NULL after conversion:
-- ALTER TABLE dbo.books ALTER COLUMN title NVARCHAR(255) NOT NULL;

-- Note: If your database collation needs changing for sorting/compare, consider altering database collation separately.

