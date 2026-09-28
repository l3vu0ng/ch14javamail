-- Script tạo bảng User cho bài tập Email List
-- Lưu ý: Trên Cloud (như Clever Cloud/Aiven), bạn đã ở sẵn trong database được cấp,
-- không cần và không được dùng lệnh CREATE DATABASE.

CREATE TABLE IF NOT EXISTS User (
    UserID INT NOT NULL AUTO_INCREMENT,
    Email VARCHAR(100) NOT NULL,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    DateCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (UserID)
);

-- Dữ liệu mẫu ban đầu
INSERT INTO User (Email, FirstName, LastName) VALUES
('johnsmith@hotmail.com', 'John', 'Smith'),
('andrea@murach.com', 'Andrea', 'Steelman')
ON DUPLICATE KEY UPDATE UserID=UserID;
