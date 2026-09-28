# Hướng Dẫn Chi Tiết: Gửi Gmail Thật, Host Database & Triển Khai Web (Chapter 14 JavaMail)

Tài liệu này hướng dẫn bạn từng bước cách cấu hình **Gửi Gmail thật**, **Host Database MySQL online miễn phí**, và **Deploy ứng dụng web lên Internet** nhưng vẫn giữ trọn vẹn cấu trúc cốt lõi chuẩn sách giáo trình Murach Java Servlets/JSP.

---

## 1. Cấu hình gửi Gmail thật bằng App Password (Mật khẩu ứng dụng)

Google không cho phép đăng nhập bằng mật khẩu tài khoản Gmail thông thường từ các ứng dụng bên thứ ba vì lý do bảo mật. Bạn cần tạo một **Mật khẩu ứng dụng (App Password)** gồm 16 ký tự:

### Các bước tạo Google App Password:
1. Đăng nhập vào tài khoản Google của bạn tại: [https://myaccount.google.com/](https://myaccount.google.com/)
2. Vào mục **Bảo mật (Security)**.
3. Đảm bảo bạn đã bật **Xác minh 2 bước (2-Step Verification)**.
4. Truy cập trực tiếp vào trang Mật khẩu ứng dụng: [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
5. Nhập tên ứng dụng, ví dụ: `JavaMail Murach` -> Bấm **Tạo (Create)**.
6. Google sẽ hiển thị một mật khẩu gồm 16 chữ cái (dạng `abcd efgh ijkl mnop`). Hãy sao chép chuỗi này (bỏ dấu cách, ví dụ: `abcdefghijklmnop`).

### Cách áp dụng vào dự án:
Lớp [MailUtilGmail](file:///e:/LapTrinhWeb/ch14javamail/src/main/java/murach/util/MailUtilGmail.java) đã được lập trình sẵn để tự động đọc thông tin từ:
- **Biến môi trường (Environment Variables)**:
  - `GMAIL_USERNAME`: Địa chỉ Gmail của bạn (ví dụ: `your_email@gmail.com`).
  - `GMAIL_APP_PASSWORD`: Mật khẩu ứng dụng 16 ký tự vừa tạo ở trên.
- **Hoặc khi test cục bộ trên Windows (PowerShell/CMD)** trước khi chạy Tomcat:
  ```powershell
  $env:GMAIL_USERNAME = "your_email@gmail.com"
  $env:GMAIL_APP_PASSWORD = "abcdefghijklmnop"
  ```
- **Hoặc truyền vào NetBeans / Tomcat VM options**:
  `-Dmail.gmail.username=your_email@gmail.com -Dmail.gmail.password=abcdefghijklmnop`

---

## 2. Host Cơ sở dữ liệu MySQL trên Cloud (Miễn phí 100%)

Để ứng dụng có thể lưu trữ dữ liệu thật trên Internet mà không phụ thuộc vào MySQL trên máy tính cá nhân của bạn, bạn có thể sử dụng các nhà cung cấp MySQL Cloud miễn phí:

### Lựa chọn 1: Aiven MySQL (Đề xuất - Miễn phí không cần thẻ tín dụng)
1. Truy cập [https://aiven.io/](https://aiven.io/) và đăng ký tài khoản miễn phí.
2. Tạo mới một dịch vụ **MySQL** (chọn gói Free Plan).
3. Aiven sẽ cấp cho bạn các thông số:
   - `Host` (ví dụ: `mysql-xxxx.aivencloud.com`)
   - `Port` (ví dụ: `12345`)
   - `User` (thường là `avnadmin`)
   - `Password`
   - `Database Name` (thường là `defaultdb`)
4. Mở công cụ quản lý DB (như DBeaver, MySQL Workbench, hoặc phpMyAdmin) kết nối vào DB Aiven, mở file [murach_email.sql](file:///e:/LapTrinhWeb/ch14javamail/sql/murach_email.sql) và bấm **Run** để tạo bảng `User`.

### Lựa chọn 2: Clever Cloud (Miễn phí 1 Database MySQL)
1. Đăng ký tại [https://www.clever-cloud.com/](https://www.clever-cloud.com/).
2. Chọn **Create** -> **an add-on** -> Chọn **MySQL** (gói Dev miễn phí).
3. Clever Cloud sẽ cấp ngay `Host`, `Database`, `User`, `Password`.
4. Import script [murach_email.sql](file:///e:/LapTrinhWeb/ch14javamail/sql/murach_email.sql).

---

## 3. Cấu hình Kết nối thông minh trong Dự án

Lớp [ConnectionPool](file:///e:/LapTrinhWeb/ch14javamail/src/main/java/murach/data/ConnectionPool.java) đã được thiết kế 2 tầng (Dual-mode):
1. **Chế độ Tomcat Local (JNDI)**: Khi chạy trong NetBeans với Tomcat cục bộ, ứng dụng tự động dùng DataSource `jdbc/murach` khai báo trong [context.xml](file:///e:/LapTrinhWeb/ch14javamail/src/main/webapp/META-INF/context.xml).
2. **Chế độ Cloud Hosting (Fallback)**: Khi deploy lên hosting bên ngoài không có JNDI, `ConnectionPool` sẽ tự động đọc các biến môi trường:
   - `DB_URL`: Ví dụ `jdbc:mysql://mysql-xxxx.aivencloud.com:12345/defaultdb?useSSL=true`
   - `DB_USER`: Tên user của Cloud DB.
   - `DB_PASSWORD`: Mật khẩu của Cloud DB.

---

## 4. Cách Deploy ứng dụng Web lên Internet

Dự án đã có sẵn [Dockerfile](file:///e:/LapTrinhWeb/ch14javamail/Dockerfile) chuẩn Tomcat 10 / JDK 17. Bạn có thể deploy rất nhanh lên các nền tảng sau:

### Cách 1: Deploy lên Render (Miễn phí qua Docker Web Service)
1. Đẩy mã nguồn bài tập lên tài khoản GitHub của bạn:
   ```bash
   git add .
   git commit -m "Complete ch14javamail project"
   git push origin main
   ```
2. Đăng nhập vào [https://render.com/](https://render.com/).
3. Bấm **New** -> **Web Service** -> Chọn Repository GitHub của bạn.
4. Render sẽ tự động phát hiện `Dockerfile`.
5. Vào phần **Environment Variables**, thêm các biến sau:
   - `GMAIL_USERNAME`: `your_email@gmail.com`
   - `GMAIL_APP_PASSWORD`: Mật khẩu ứng dụng 16 ký tự của bạn
   - `DB_URL`: URL kết nối MySQL trên cloud (từ Aiven/Clever Cloud)
   - `DB_USER`: Tên đăng nhập DB
   - `DB_PASSWORD`: Mật khẩu DB
6. Bấm **Deploy Web Service**. Sau vài phút, Render sẽ cấp cho bạn một đường link công khai dạng `https://ch14javamail-xxxx.onrender.com` để truy cập và nộp bài!

### Cách 2: Deploy lên Clever Cloud (Java Tomcat)
1. Tạo ứng dụng **Java** trên Clever Cloud.
2. Chọn Java 17 và container Tomcat.
3. Cấu hình biến môi trường tương tự như trên.
4. Đẩy Git hoặc upload file `target/ch14javamail-1.0.war`.

---

## 5. Kiểm tra ứng dụng hoạt động
1. Truy cập vào trang web `http://localhost:8080/ch14javamail/` (nếu chạy local) hoặc URL hosting của bạn.
2. Nhập thông tin: Email, First Name, Last Name.
3. Chọn Mail Server: **Gmail SMTP**.
4. Bấm **Join Now**:
   - Dữ liệu người dùng sẽ được lưu vào cơ sở dữ liệu MySQL (bảng `User`).
   - Một email chào mừng thực tế sẽ được gửi từ tài khoản Gmail của bạn tới email của người dùng.
   - Trang `thanks.jsp` sẽ hiển thị thông báo thành công màu xanh lá, kèm theo thông tin đã đăng ký.
