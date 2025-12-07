package com.example.haui5s;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etMaSV, etPass;
    Button btnLogin;
    TextView btnGoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etMaSV = findViewById(R.id.etMaSV);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        btnGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String user = etMaSV.getText().toString().trim();
        String pass = etPass.getText().toString().trim();

        if(user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // TẠM THỜI BỎ JDBC - DÙNG LOGIC ĐƠN GIẢN
        if ((user.equals("SV001") && pass.equals("123456")) ||
                (user.equals("GV001") && pass.equals("123456"))) {

            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

            // Lưu session
            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("masv", user);
            editor.apply();

            Intent intent;
            if (user.toUpperCase().startsWith("GV")) {
                intent = new Intent(LoginActivity.this, TeacherHomeActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, StudentHomeActivity.class);
            }

            intent.putExtra("student_code", user);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
        }

        /*
        // CODE JDBC ORIGINAL (COMMENT LẠI TẠM THỜI)
        JDBCService.checkLogin(user, pass, success -> {
            runOnUiThread(() -> {
                if(success) {
                    // ... success code ...
                } else {
                    Toast.makeText(LoginActivity.this, "Sai tên đăng nhập hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            });
        });
        */
    }
}