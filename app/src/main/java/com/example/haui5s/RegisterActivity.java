package com.example.haui5s;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.haui5s.JDBCService;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    EditText etMaSV, etTen, etPass, etLop, etSDT, etEmail, etSecretCode;
    Spinner spNganh, spKhoa;
    Button btnRegister;
    CheckBox cbTerms, cbIsTeacher;
    LinearLayout layoutStudentInfo;
    TextInputLayout layoutSecretCode;

    private static final String ADMIN_SECRET_CODE = "HAUI@2025";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etMaSV = findViewById(R.id.regMaSV);
        etTen = findViewById(R.id.regTen);
        etPass = findViewById(R.id.regPass);
        etLop = findViewById(R.id.regLop);
        etSDT = findViewById(R.id.regSDT);
        etEmail = findViewById(R.id.regEmail);
        etSecretCode = findViewById(R.id.regSecretCode);

        spNganh = findViewById(R.id.spNganh);
        spKhoa = findViewById(R.id.spKhoa);
        btnRegister = findViewById(R.id.btnRegister);
        cbTerms = findViewById(R.id.cbTerms);
        cbIsTeacher = findViewById(R.id.cbIsTeacher);

        layoutStudentInfo = findViewById(R.id.layoutStudentInfo);
        layoutSecretCode = findViewById(R.id.layoutSecretCode);

        ArrayAdapter<CharSequence> adapterNganh = ArrayAdapter.createFromResource(this,
                R.array.chuyen_nganh_array, android.R.layout.simple_spinner_item);
        adapterNganh.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNganh.setAdapter(adapterNganh);

        ArrayAdapter<CharSequence> adapterKhoa = ArrayAdapter.createFromResource(this,
                R.array.khoa_array, android.R.layout.simple_spinner_item);
        adapterKhoa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKhoa.setAdapter(adapterKhoa);

        spNganh.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedText = parent.getItemAtPosition(position).toString();
                if(selectedText.contains("(") && selectedText.contains(")")) {
                    String maNganh = selectedText.substring(selectedText.lastIndexOf("(") + 1, selectedText.lastIndexOf(")"));
                    etLop.setText(maNganh);
                    etLop.setSelection(etLop.getText().length());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cbIsTeacher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutStudentInfo.setVisibility(View.GONE);
                layoutSecretCode.setVisibility(View.VISIBLE);
                etMaSV.setHint("Mã Giảng Viên (Ví dụ: GV001)");
            } else {
                layoutStudentInfo.setVisibility(View.VISIBLE);
                layoutSecretCode.setVisibility(View.GONE);
                etMaSV.setHint("Mã Sinh Viên");
            }
        });

        btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String ma = etMaSV.getText().toString().trim();
        String ten = etTen.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        String sdt = etSDT.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String lop = etLop.getText().toString().trim();
        String nganh = spNganh.getSelectedItem().toString();
        String khoa = spKhoa.getSelectedItem().toString();
        String roleMsg;

        if (cbIsTeacher.isChecked()) {
            String inputSecret = etSecretCode.getText().toString().trim();

            if (!inputSecret.equals(ADMIN_SECRET_CODE)) {
                Toast.makeText(this, "Mã bảo mật Giáo viên KHÔNG ĐÚNG!", Toast.LENGTH_LONG).show();
                etSecretCode.setError("Sai mã bảo mật!");
                return;
            }

            if (!ma.toUpperCase().startsWith("GV")) {
                ma = "GV" + ma;
            }
            lop = "Giảng viên";
            nganh = "Giảng viên";
            khoa = "GV";
            roleMsg = "Giáo viên";

        } else {
            if (lop.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập Lớp!", Toast.LENGTH_SHORT).show();
                return;
            }
            roleMsg = "Sinh viên";
        }

        if(ma.isEmpty() || ten.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!cbTerms.isChecked()) {
            Toast.makeText(this, "Bạn chưa chấp nhận điều khoản!", Toast.LENGTH_SHORT).show();
            return;
        }

        final String finalMa = ma;
        final String finalLop = lop;
        final String finalNganh = nganh;
        final String finalKhoa = khoa;
        final String finalRoleMsg = roleMsg;
        final String finalPass = pass;
        final String finalTen = ten;
        final String finalSDT = sdt;
        final String finalEmail = email;

        JDBCService.checkMaSV(finalMa, exists -> {
            if(exists) {
                Toast.makeText(RegisterActivity.this, "Tài khoản đã tồn tại!", Toast.LENGTH_SHORT).show();
                return;
            }

            JDBCService.insertData(finalMa, finalPass, finalTen, finalNganh, finalLop, finalKhoa, finalSDT, finalEmail, success -> {
                if(success) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký " + finalRoleMsg + " thành công!\nTài khoản: " + finalMa, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}