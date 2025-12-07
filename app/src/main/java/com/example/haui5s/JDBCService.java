package com.example.haui5s;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JDBCService {

    // SỬA URL: BỎ characterEncoding=utf8, chỉ dùng useUnicode=true
    private static final String DB_URL = "jdbc:mysql://10.0.2.2:3306/HAUI5S?useUnicode=true";
    private static final String USER = "root";
    private static final String PASS = "Huybinh2005@";

    public static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static Connection getConnection() throws SQLException {
        try {
            // Dùng driver cũ MySQL 5.x
            Class.forName("com.mysql.jdbc.Driver");
            Log.d("JDBC", "Đang kết nối đến: " + DB_URL);
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Log.d("JDBC", "Kết nối thành công");
            return conn;
        } catch (ClassNotFoundException e) {
            Log.e("JDBC", "Không tìm thấy driver: " + e.getMessage());
            throw new SQLException("Lỗi: Không tìm thấy Driver MySQL.", e);
        } catch (SQLException e) {
            Log.e("JDBC", "Lỗi kết nối SQL: " + e.getMessage());
            throw e;
        }
    }

    public interface SimpleCallback {
        void onResult(boolean success);
    }

    public interface LoginCallback {
        void onLoginResult(boolean success);
    }

    public interface UserInfoCallback {
        void onInfoLoaded(UserInfoModel user);
    }

    public interface BorrowListCallback {
        void onListLoaded(List<BorrowModel> list);
    }

    public static void testConnection(SimpleCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            try (Connection conn = getConnection()) {
                success = conn != null && !conn.isClosed();
                Log.d("JDBC_TEST", "Kết nối thành công: " + success);
            } catch (SQLException e) {
                Log.e("JDBC_TEST", "Lỗi kết nối: " + e.getMessage());
            }
            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onResult(finalSuccess));
        });
    }

    public static void checkLogin(String userCode, String password, LoginCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String SQL = "SELECT COUNT(*) FROM users WHERE user_code = ? AND password = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {

                stmt.setString(1, userCode);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        success = true;
                    }
                }
            } catch (SQLException e) {
                Log.e("JDBC_LOGIN", "Lỗi DB khi đăng nhập: " + e.getMessage());
            }

            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onLoginResult(finalSuccess));
        });
    }

    public static void checkMaSV(String userCode, SimpleCallback callback) {
        executor.execute(() -> {
            boolean exists = false;
            String SQL = "SELECT COUNT(*) FROM users WHERE user_code = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {

                stmt.setString(1, userCode);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        exists = true;
                    }
                }
            } catch (SQLException e) {
                Log.e("JDBC_CHECK", "Lỗi DB khi kiểm tra mã: " + e.getMessage());
            }

            final boolean finalExists = exists;
            mainHandler.post(() -> callback.onResult(finalExists));
        });
    }

    public static void insertData(String ma, String pass, String ten, String nganh, String lop, String khoa, String sdt, String email, SimpleCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String SQL = "INSERT INTO users (user_code, password, full_name, major, class, course, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {

                stmt.setString(1, ma);
                stmt.setString(2, pass);
                stmt.setString(3, ten);
                stmt.setString(4, nganh);
                stmt.setString(5, lop);
                stmt.setString(6, khoa);
                stmt.setString(7, sdt);
                stmt.setString(8, email);

                success = stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                Log.e("JDBC_INSERT", "Lỗi DB khi đăng ký: " + e.getMessage());
            }

            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onResult(finalSuccess));
        });
    }

    public static void getUserInfo(String userCode, UserInfoCallback callback) {
        executor.execute(() -> {
            UserInfoModel user = null;
            String SQL = "SELECT full_name, major, class, course, phone, email FROM users WHERE user_code = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, userCode);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        user = new UserInfoModel(
                                userCode,
                                rs.getString("full_name"),
                                rs.getString("major"),
                                rs.getString("class"),
                                rs.getString("course"),
                                rs.getString("phone"),
                                rs.getString("email")
                        );
                    }
                }
            } catch (SQLException e) {
                Log.e("JDBC_INFO", "Lỗi DB khi lấy thông tin user: " + e.getMessage());
            }

            final UserInfoModel finalUser = user;
            mainHandler.post(() -> callback.onInfoLoaded(finalUser));
        });
    }

    public static void getBorrowListByDate(String date, BorrowListCallback callback) {
        executor.execute(() -> {
            List<BorrowModel> list = new ArrayList<>();
            String SQL = "SELECT borrow_id, student_name, tool_name, borrow_date, status FROM borrow_list WHERE DATE(borrow_date) = STR_TO_DATE(?, '%d/%m/%Y')";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {

                stmt.setString(1, date);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new BorrowModel(
                                rs.getInt("borrow_id"),
                                rs.getString("student_name"),
                                rs.getString("tool_name"),
                                rs.getString("borrow_date"),
                                rs.getInt("status")
                        ));
                    }
                }
            } catch (SQLException e) {
                Log.e("JDBC_BORROW", "Lỗi DB khi lấy danh sách mượn: " + e.getMessage());
            }

            mainHandler.post(() -> callback.onListLoaded(list));
        });
    }

    public static void insertBorrow(String maSv, String name, String toolList, String date, int status, SimpleCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String SQL = "INSERT INTO borrow_list (student_code, student_name, tool_name, borrow_date, status) VALUES (?, ?, ?, STR_TO_DATE(?, '%d/%m/%Y %H:%i:%s'), ?)";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {

                stmt.setString(1, maSv);
                stmt.setString(2, name);
                stmt.setString(3, toolList);
                stmt.setString(4, date);
                stmt.setInt(5, status);

                success = stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                Log.e("JDBC_INSERT_BORROW", "Lỗi DB khi chèn phiếu mượn: " + e.getMessage());
            }

            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onResult(finalSuccess));
        });
    }

    public static void updateBorrowStatus(int borrowId, int newStatus, SimpleCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String SQL = "UPDATE borrow_list SET status = ? WHERE borrow_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {

                stmt.setInt(1, newStatus);
                stmt.setInt(2, borrowId);

                success = stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                Log.e("JDBC_UPDATE_STATUS", "Lỗi DB khi cập nhật trạng thái: " + e.getMessage());
            }

            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onResult(finalSuccess));
        });
    }
}