package com.example.haui5s;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    TextView tvName, tvID, tvLop, tvNganh, tvEmail;
    Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvProfileName);
        tvID = view.findViewById(R.id.tvProfileID);
        tvLop = view.findViewById(R.id.tvDetailLop);
        tvNganh = view.findViewById(R.id.tvDetailNganh);
        tvEmail = view.findViewById(R.id.tvDetailEmail);
        btnLogout = view.findViewById(R.id.btnLogout);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String masv = sharedPreferences.getString("masv", "");

        if (masv.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy phiên đăng nhập!", Toast.LENGTH_SHORT).show();
            return view;
        }

        tvID.setText("Mã SV: " + masv);
        tvName.setText("Đang tải dữ liệu...");

        JDBCService.getUserInfo(masv, user -> {
            if (!isAdded() || getContext() == null) {
                return;
            }

            if (user != null) {
                tvName.setText(user.fullName != null ? user.fullName : "Chưa cập nhật");

                String lopHoc = user.classInfo != null ? user.classInfo : "";
                String khoaHoc = user.course != null ? user.course : "";
                tvLop.setText("Lớp: " + lopHoc + " - " + khoaHoc);

                tvNganh.setText("Ngành: " + (user.major != null ? user.major : ""));
                tvEmail.setText("Email: " + (user.email != null ? user.email : ""));
            } else {
                tvName.setText("Lỗi tải dữ liệu");
                Toast.makeText(getContext(), "Không thể lấy thông tin cá nhân từ Server!", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}