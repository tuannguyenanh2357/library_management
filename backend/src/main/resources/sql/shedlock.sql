CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until DATETIME NOT NULL,
    locked_at DATETIME NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
