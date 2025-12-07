package com.example.haui5s;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class StudentHomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    public String currentMaSV = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        // 1. Nhận mã sinh viên (Ưu tiên Intent, nếu null thì lấy từ Session)
        currentMaSV = getIntent().getStringExtra("student_code");
        if (currentMaSV == null || currentMaSV.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            currentMaSV = prefs.getString("masv", "");
        }

        // 2. Ánh xạ đúng ID trong activity_student_home.xml
        bottomNav = findViewById(R.id.bottom_nav_student);

        // 3. Xử lý sự kiện Menu
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_home_student) {
                    // Nếu chưa có HomeFragment, dùng tạm ToolsFragment
                    selectedFragment = new ToolsFragment();
                }
                else if (id == R.id.nav_calendar_student) {
                    // Chức năng Lịch/Mượn đồ
                    selectedFragment = new ToolsFragment();
//                }
//                else if (id == R.id.nav_report_student) {
//                    // --- QUAN TRỌNG: CHUYỂN SANG MÀN HÌNH BÁO CÁO (Activity) ---
////                    Intent intent = new Intent(StudentHomeActivity.this, ReportActivity.class);
//                    intent.putExtra("student_code", currentMaSV);
//                    startActivity(intent);
//                    return false; // Không đổi trạng thái tab, giữ nguyên tab hiện tại
                }
                else if (id == R.id.nav_profile_student) {
                    // Nếu chưa có ProfileFragment, dùng tạm ToolsFragment tránh lỗi
                    selectedFragment = new ToolsFragment();
                }

                // Thay thế Fragment vào khung chứa
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container_student, selectedFragment)
                            .commit();
                }
                return true;
            }
        });

        // 4. Mặc định chạy màn hình đầu tiên khi mở
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_student, new ToolsFragment())
                    .commit();
            // Đánh dấu icon Home (hoặc Calendar) sáng lên
            bottomNav.setSelectedItemId(R.id.nav_home_student);
        }
    }

    // Hàm public để các Fragment con có thể lấy Mã SV nếu cần
    public String getMyMaSV() {
        return currentMaSV;
    }
}