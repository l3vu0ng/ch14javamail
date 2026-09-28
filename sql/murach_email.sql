-- Script khởi tạo cơ sở dữ liệu murach và bảng User cho bài tập Email List
CREATE DATABASE IF NOT EXISTS murach;
USE murach;

CREATE TABLE IF NOT EXISTS User (
    UserID INT NOT NULL AUTO_INCREMENT,
    Email VARCHAR(100) NOT NULL,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    DateCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (UserID)
);

-- Dữ liệu mẫu (nếu cần)
INSERT INTO User (Email, FirstName, LastName) VALUES
('johnsmith@hotmail.com', 'John', 'Smith'),
('andrea@murach.com', 'Andrea', 'Steelman')
ON DUPLICATE KEY UPDATE UserID=UserID;
