package com.example.haui5s;

import android.os.Handler;
import android.os.Looper;
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

    private static final String DB_URL = "jdbc:mysql://10.0.2.2:3306/HAUI5S?characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASS = "Huybinh2005@";

    public static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Lỗi Driver MySQL", e);
        }
    }

    // --- CÁC INTERFACE CALLBACK ---
    public interface SimpleCallback { void onResult(boolean success); }
    public interface LoginCallback { void onLoginResult(boolean success); }
    public interface UserInfoCallback { void onInfoLoaded(UserInfoModel user); }
    public interface BorrowListCallback { void onListLoaded(List<BorrowModel> list); }
    public interface ScheduleListCallback { void onLoaded(List<ScheduleModel> schedules); }
    public interface ReportListCallback { void onLoaded(List<ReportModel> list); }

    // --- 1. XỬ LÝ USER (Đăng nhập/Đăng ký/Lấy tin) ---
    public static void getUserInfo(String userCode, UserInfoCallback callback) {
        executor.execute(() -> {
            UserInfoModel user = null;
            String SQL = "SELECT full_name, major, class, course, phone, email FROM users WHERE user_code = ?";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, userCode);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        user = new UserInfoModel(userCode, rs.getString("full_name"), rs.getString("major"), rs.getString("class"), rs.getString("course"), rs.getString("phone"), rs.getString("email"));
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
            final UserInfoModel finalUser = user;
            mainHandler.post(() -> callback.onInfoLoaded(finalUser));
        });
    }

    public static void checkLogin(String userCode, String password, LoginCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String SQL = "SELECT COUNT(*) FROM users WHERE user_code = ? AND password = ?";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, userCode);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) success = true;
                }
            } catch (SQLException e) { e.printStackTrace(); }
            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onLoginResult(finalSuccess));
        });
    }

    public static void insertData(String ma, String pass, String ten, String nganh, String lop, String khoa, String sdt, String email, SimpleCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String SQL = "INSERT INTO users (user_code, password, full_name, major, class, course, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, ma); stmt.setString(2, pass); stmt.setString(3, ten); stmt.setString(4, nganh);
                stmt.setString(5, lop); stmt.setString(6, khoa); stmt.setString(7, sdt); stmt.setString(8, email);
                success = stmt.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); }
            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onResult(finalSuccess));
        });
    }

    public static void checkMaSV(String userCode, SimpleCallback callback) {
        executor.execute(() -> {
            boolean exists = false;
            String SQL = "SELECT COUNT(*) FROM users WHERE user_code = ?";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, userCode);
                try (ResultSet rs = stmt.executeQuery()) { if (rs.next() && rs.getInt(1) > 0) exists = true; }
            } catch (SQLException e) { e.printStackTrace(); }
            final boolean finalExists = exists;
            mainHandler.post(() -> callback.onResult(finalExists));
        });
    }

    // --- 2. XỬ LÝ MƯỢN TRẢ (Cập nhật có student_code) ---
    public static void getBorrowListByDate(String date, BorrowListCallback callback) {
        executor.execute(() -> {
            List<BorrowModel> list = new ArrayList<>();
            // Đã thêm cột student_code vào SQL
            String SQL = "SELECT borrow_id, student_code, student_name, tool_name, borrow_date, status FROM borrow_list WHERE borrow_date LIKE ?";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, date + "%");
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new BorrowModel(
                                rs.getInt("borrow_id"),
                                rs.getString("student_code"), // Cột mới thêm
                                rs.getString("student_name"),
                                rs.getString("tool_name"),
                                rs.getString("borrow_date"),
                                rs.getInt("status")
                        ));
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
            mainHandler.post(() -> callback.onListLoaded(list));
        });
    }

    public static void insertBorrow(String maSv, String name, String toolList, String date, int status, SimpleCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String SQL = "INSERT INTO borrow_list (student_code, student_name, tool_name, borrow_date, status) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, maSv); stmt.setString(2, name); stmt.setString(3, toolList); stmt.setString(4, date); stmt.setInt(5, status);
                success = stmt.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); }
            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onResult(finalSuccess));
        });
    }

    public static void updateBorrowStatus(int borrowId, int newStatus, SimpleCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String SQL = "UPDATE borrow_list SET status = ? WHERE borrow_id = ?";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setInt(1, newStatus); stmt.setInt(2, borrowId);
                success = stmt.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); }
            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onResult(finalSuccess));
        });
    }

    // --- 3. XỬ LÝ LỊCH TRỰC NHẬT ---
    public static void insertCleaningSchedule(String code, String name, String classInfo, String area, String note, String dateTime, SimpleCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String SQL = "INSERT INTO cleaning_schedule (person_code, person_name, class_info, area, note, schedule_date, status) VALUES (?, ?, ?, ?, ?, STR_TO_DATE(?, '%d/%m/%Y %H:%i'), 0)";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, code); stmt.setString(2, name); stmt.setString(3, classInfo);
                stmt.setString(4, area); stmt.setString(5, note); stmt.setString(6, dateTime);
                success = stmt.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); }
            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onResult(finalSuccess));
        });
    }

    public static void getAllSchedules(ScheduleListCallback callback) {
        executor.execute(() -> {
            List<ScheduleModel> list = new ArrayList<>();
            String SQL = "SELECT schedule_id, person_name, area, note, schedule_date FROM cleaning_schedule ORDER BY schedule_date ASC";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(new ScheduleModel(rs.getInt("schedule_id"), rs.getString("person_name"), rs.getString("area"), rs.getString("note"), rs.getString("schedule_date")));
            } catch (SQLException e) { e.printStackTrace(); }
            mainHandler.post(() -> callback.onLoaded(list));
        });
    }

    // --- 4. XỬ LÝ CHẤM ĐIỂM 5S (ReportModel) ---
    public static void insertReport(String code, String name, String area, String note, String image, SimpleCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            String SQL = "INSERT INTO report_log (reporter_code, reporter_name, area, note, image_url, report_timestamp, status) VALUES (?, ?, ?, ?, ?, NOW(), 0)";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, code); stmt.setString(2, name); stmt.setString(3, area);
                stmt.setString(4, note); stmt.setString(5, image);
                success = stmt.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); }
            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onResult(finalSuccess));
        });
    }

    public static void getReportList(String userCode, boolean isTeacher, ReportListCallback callback) {
        executor.execute(() -> {
            List<ReportModel> list = new ArrayList<>();
            String SQL;
            if (isTeacher) {
                SQL = "SELECT * FROM report_log ORDER BY report_timestamp DESC";
            } else {
                SQL = "SELECT * FROM report_log WHERE reporter_code = ? ORDER BY report_timestamp DESC";
            }
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
                if (!isTeacher) { stmt.setString(1, userCode); }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new ReportModel(
                                rs.getInt("report_id"), rs.getString("reporter_code"), rs.getString("reporter_name"),
                                rs.getString("area"), rs.getString("note"), rs.getString("image_url"),
                                rs.getString("report_timestamp"), rs.getInt("status"),
                                rs.getString("handler_code"), rs.getString("resolution_note"),
                                rs.getInt("score_s1"), rs.getInt("score_s2"), rs.getInt("score_s3"),
                                rs.getInt("score_s4"), rs.getInt("score_s5"), rs.getInt("final_evaluation")
                        ));
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
            mainHandler.post(() -> callback.onLoaded(list));
        });
    }

    public static void updateReportStatus(int reportId, String handlerCode, String resolutionNote, int s1, int s2, int s3, int s4, int s5, SimpleCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            int avg = Math.round((s1 + s2 + s3 + s4 + s5) / 5.0f);
            String SQL = "UPDATE report_log SET status = 1, handler_code = ?, resolution_note = ?, resolution_timestamp = NOW(), score_s1 = ?, score_s2 = ?, score_s3 = ?, score_s4 = ?, score_s5 = ?, final_evaluation = ? WHERE report_id = ?";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, handlerCode); stmt.setString(2, resolutionNote);
                stmt.setInt(3, s1); stmt.setInt(4, s2); stmt.setInt(5, s3); stmt.setInt(6, s4); stmt.setInt(7, s5);
                stmt.setInt(8, avg); stmt.setInt(9, reportId);
                success = stmt.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); }
            final boolean finalSuccess = success;
            mainHandler.post(() -> callback.onResult(finalSuccess));
        });
    }
}