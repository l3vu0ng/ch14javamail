package murach.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionPool {

    private static ConnectionPool pool = null;
    private static DataSource dataSource = null;
    private static String jndiError = null;

    private ConnectionPool() {
        try {
            InitialContext ic = new InitialContext();
            try {
                dataSource = (DataSource) ic.lookup("java:comp/env/jdbc/murach");
            } catch (NamingException ne) {
                dataSource = (DataSource) ic.lookup("java:/comp/env/jdbc/murach");
            }
        } catch (NamingException e) {
            jndiError = e.getMessage();
            System.out.println("ConnectionPool JNDI note (using environment fallback if needed): " + e.getMessage());
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if (pool == null) {
            pool = new ConnectionPool();
        }
        return pool;
    }

    public Connection getConnection() throws SQLException {
        // 1. Thử lấy connection từ JNDI DataSource nếu có
        if (dataSource != null) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                System.err.println("DataSource getConnection failed, trying fallback: " + e.getMessage());
            }
        }

        // 2. Fallback: Hỗ trợ kết nối trực tiếp khi hosting trên Cloud hoặc chạy ngoài JNDI
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("DATABASE_URL");
        }
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("MYSQL_URL");
        }
        if (dbUrl == null || dbUrl.isBlank()) {
            // Cấu hình mặc định cục bộ
            dbUrl = "jdbc:mysql://localhost:3306/murach?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }

        String dbUser = System.getenv("DB_USER");
        if (dbUser == null || dbUser.isBlank()) {
            dbUser = "root";
        }

        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null) {
            dbPassword = "0774551185aA";
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (ClassNotFoundException cnfe) {
            throw new SQLException("MySQL JDBC Driver not found: " + cnfe.getMessage(), cnfe);
        }
    }

    public void freeConnection(Connection c) {
        try {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
