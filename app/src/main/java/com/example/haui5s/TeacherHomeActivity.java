package com.example.haui5s;

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

        currentMaGV = getIntent().getStringExtra("USERNAME");
        if(currentMaGV == null) currentMaGV = "GV001";

        bottomNav = findViewById(R.id.bottom_nav_teacher);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_teacher, new HomeFragment()).commit();

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_home_teacher) {
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.nav_tools_teacher) {
                    selectedFragment = new ToolsFragment();
                } else if (id == R.id.nav_calendar_teacher) {
                    selectedFragment = new CalendarFragment();
                } else if (id == R.id.nav_profile_teacher) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container_teacher, selectedFragment)
                            .commit();
                }
                return true;
            }
        });
    }

    public String getMyMaSV() {
        return currentMaGV;
    }

}