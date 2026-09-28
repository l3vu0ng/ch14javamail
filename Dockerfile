# Sử dụng image Tomcat 10 chính thức với JDK 17 (tương thích Jakarta EE 10/11)
FROM tomcat:10.1-jdk17-temurin

# Xóa các app mặc định trong webapps của Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy file WAR đã đóng gói vào thư mục webapps của Tomcat với tên ROOT.war để chạy trực tiếp tại đường dẫn gốc /
COPY target/ch14javamail-1.0.war /usr/local/tomcat/webapps/ROOT.war

# Mở cổng 8080
EXPOSE 8080

# Chạy Tomcat
CMD ["catalina.sh", "run"]
