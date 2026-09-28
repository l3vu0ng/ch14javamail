# Thiết Kế Chi Tiết Bài Tập Chapter 14: JavaMail & Database Hosting (Chuẩn Murach)

## 1. Mục tiêu & Yêu cầu mở rộng
- **Core giáo trình Murach**: Tuân thủ cấu trúc package, mã nguồn và luồng xử lý chuẩn trong giáo trình Murach (Slide 21 đến Slide 28 của `Chapter 14 slides.pptx`).
- **Gửi Gmail thật**: Triển khai `MailUtilGmail` gửi email thực tế qua máy chủ `smtp.gmail.com` với App Password (Mật khẩu ứng dụng 16 ký tự của Google) bằng giao thức TLS (port 587) / SSL (port 465).
- **Hỗ trợ Hosting & Database thật**:
  - Tích hợp lưu trữ vào cơ sở dữ liệu MySQL (bảng `User`) qua `UserDB`, `ConnectionPool`, `DBUtil`.
  - Hỗ trợ cả 2 chế độ kết nối: JNDI DataSource trên Tomcat (`jdbc/murach`) và nạp trực tiếp từ Biến môi trường (`DB_URL`, `DB_USER`, `DB_PASSWORD` hoặc `DATABASE_URL`) giúp dễ dàng deploy lên các nền tảng hosting đám mây (Clever Cloud, Railway, Render, VPS...).
  - Tương tự, cấu hình email hỗ trợ biến môi trường (`GMAIL_USERNAME`, `GMAIL_APP_PASSWORD`) hoặc `web.xml` context-param để tránh lộ mật khẩu trong code khi đẩy lên GitHub / hosting.

---

## 2. Kiến trúc & Các thành phần (Components)

```
src/main/java/
├── murach/
│   ├── business/
│   │   └── User.java                 # JavaBean đại diện cho User (email, firstName, lastName)
│   ├── data/
│   │   ├── ConnectionPool.java       # Quản lý kết nối DB (hỗ trợ JNDI + fallback Environment Variables)
│   │   ├── DBUtil.java               # Đóng Statement, PreparedStatement, ResultSet an toàn
│   │   └── UserDB.java               # Lưu thông tin User vào bảng User trong MySQL
│   ├── util/
│   │   ├── MailUtilLocal.java        # Gửi qua Local SMTP server (localhost:25) chuẩn Slide 21-22
│   │   └── MailUtilGmail.java        # Gửi qua Gmail SMTP thực tế (smtp.gmail.com) chuẩn Slide 27-28
│   └── email/
│       └── EmailListServlet.java     # Servlet xử lý action join/add, gọi UserDB và MailUtil
```

### A. Mô hình dữ liệu & Database (Model & Data)
- **`murach.business.User`**:
  - `firstName`, `lastName`, `email`.
  - Constructors, getters, setters, implements `Serializable`.
- **`murach.data.ConnectionPool` & `DBUtil`**:
  - Ưu tiên tra cứu JNDI DataSource `jdbc/murach` từ `context.xml`.
  - Cơ chế dự phòng thông minh (Fallback): Tự động đọc từ `System.getenv("DB_URL")` (hoặc `DATABASE_URL`) khi chạy trên Cloud / Hosting không dùng JNDI.
- **`murach.data.UserDB`**:
  - `public static int insert(User user)`: Thực hiện `INSERT INTO User (Email, FirstName, LastName) VALUES (?, ?, ?)` với `PreparedStatement`.
  - `public static boolean emailExists(String email)`: Kiểm tra email đã đăng ký chưa (tiêu chuẩn Murach).
- **File SQL khởi tạo**: `sql/murach_email.sql` tạo database `murach` và bảng `User`.

### B. Tiện ích gửi Mail (JavaMail Utilities)
- **`murach.util.MailUtilGmail`**:
  - Gửi qua `smtp.gmail.com`:
    * Port 587 với `mail.smtp.starttls.enable=true` và `mail.smtp.auth=true` (hoặc Port 465 SSL).
    * Tài khoản & Mật khẩu ứng dụng: Đọc từ biến môi trường `GMAIL_USERNAME`, `GMAIL_APP_PASSWORD` hoặc nạp giá trị cấu hình mặc định.
  - Xây dựng `MimeMessage`, thiết lập tiêu đề, mã hóa UTF-8 cho tiếng Việt, hỗ trợ cả Plain Text và HTML.
- **`murach.util.MailUtilLocal`**:
  - Giữ nguyên cấu trúc phục vụ test cục bộ localhost port 25 theo đúng Slide 21-22.

### C. Bộ điều khiển (Controller - `EmailListServlet`)
- `@WebServlet("/emailList")`
- `doPost`:
  - `action == "join"`: Forward tới `/index.jsp`.
  - `action == "add"`:
    1. Nhận `firstName`, `lastName`, `email`.
    2. Lưu người dùng vào database qua `UserDB.insert(user)`.
    3. Gửi email xác nhận đến địa chỉ của user qua `MailUtilGmail.sendMail(...)`.
    4. Bắt `MessagingException`: Ghi log server, hiển thị thông báo lỗi chi tiết trên giao diện nếu chưa thiết lập App Password hoặc lỗi mạng.
    5. Forward kết quả sang `/thanks.jsp`.

### D. Giao diện người dùng (Views & Styles)
- **`src/main/webapp/index.jsp`**: Form đăng ký nhận email đẹp, chuyên nghiệp.
- **`src/main/webapp/thanks.jsp`**: Hiển thị thông tin đăng ký, kết quả gửi email thật hoặc thông báo lỗi nếu có.
- **`src/main/webapp/includes/header.html`** & **`footer.jsp`**: Bản quyền `l3vu0ng & Associates`.
- **`src/main/webapp/main.css`**: CSS chuẩn của Murach.

---

## 3. Cấu hình Thư viện (`pom.xml`)
- `jakarta.platform:jakarta.jakartaee-api:11.0.0-M1` (`provided`)
- `com.mysql:mysql-connector-j:8.3.0` (kết nối MySQL DB)
- `org.eclipse.angus:jakarta.mail:2.0.3` (runtime implementation cho JavaMail trên Tomcat)
- `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.0` & `org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1`

---

## 4. Hướng dẫn Hosting & Chạy Thực Tế (Deployment Guide)
1. **Gmail App Password**: Hướng dẫn tạo mật khẩu ứng dụng 16 ký tự trên Google Account.
2. **Database Cloud**: Hướng dẫn sử dụng MySQL miễn phí (Aiven / Clever Cloud / TiDB) hoặc MySQL cục bộ.
3. **Deploy Web App**: Hướng dẫn đóng gói file WAR và đưa lên Tomcat Hosting (Render / Clever Cloud / Railway).
