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

        currentMaSV = getIntent().getStringExtra("student_code");
        if (currentMaSV == null || currentMaSV.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            currentMaSV = prefs.getString("masv", "");
        }

        bottomNav = findViewById(R.id.bottom_nav_student);

        // Xử lý sự kiện bấm Menu (4 mục)
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_home_student) {
                    selectedFragment = new HomeFragment();
                }
                else if (id == R.id.nav_calendar_student) {
                    selectedFragment = new CalendarFragment();
                }
                else if (id == R.id.nav_report_student) {
                    selectedFragment = new ReportFragment(); // Gọi màn hình Báo cáo 5S
                }
                else if (id == R.id.nav_profile_student) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container_student, selectedFragment)
                            .commit();
                }
                return true;
            }
        });

        // Mặc định mở Trang chủ
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_student, new HomeFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.nav_home_student);
        }
    }

    public String getMyMaSV() {
        return currentMaSV;
    }
}