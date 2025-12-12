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

public class TeacherHomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    public String currentMaGV = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        // 1. Lấy mã GV từ Intent (Gửi từ LoginActivity) hoặc SharedPreferences
        currentMaGV = getIntent().getStringExtra("student_code");

        if (currentMaGV == null || currentMaGV.isEmpty()) {
            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            currentMaGV = sharedPreferences.getString("masv", "GV001");
        }

        // Ánh xạ View từ layout activity_teacher_home.xml
        bottomNav = findViewById(R.id.bottom_nav_teacher);

        // Mặc định mở HomeFragment khi vào app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_teacher, new HomeFragment())
                    .commit();
        }

        // Bắt sự kiện click vào menu dưới đáy
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_home_teacher) {
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.nav_calendar_teacher) {
                    selectedFragment = new CalendarFragment(); // Bạn cần tạo file này nếu chưa có
                } else if (id == R.id.nav_report_teacher) {
                    // Mở màn hình Báo cáo 5S (ReportFragment)
                    selectedFragment = new ReportFragment();

                } else if (id == R.id.nav_tools_teacher) {
                    selectedFragment = new ToolsFragment(); // Đảm bảo bạn đã có file ToolsFragment
                }
                else if (id == R.id.nav_profile_teacher) {
                    selectedFragment = new ProfileFragment(); // Bạn cần tạo file này nếu chưa có
                }

                // Thay thế Fragment vào khung chứa (fragment_container_teacher)
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container_teacher, selectedFragment)
                            .commit();
                }
                return true;
            }
        });
    }

    // Hàm public để các Fragment con (như ReportFragment) có thể lấy Mã GV để truy vấn DB
    public String getMyMaSV() {
        return currentMaGV;
    }
}