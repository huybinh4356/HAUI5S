package com.example.haui5s;

import android.content.Intent;
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

    private TextView tvName, tvID, tvLop, tvKhoa, tvNganh, tvEmail, tvPhone;
    private Button btnLogout;
    private String currentUserCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ View
        tvName = view.findViewById(R.id.tvProfileName);
        tvID = view.findViewById(R.id.tvProfileID);
        tvLop = view.findViewById(R.id.tvDetailLop);
        tvKhoa = view.findViewById(R.id.tvDetailKhoa);
        tvNganh = view.findViewById(R.id.tvDetailNganh);
        tvEmail = view.findViewById(R.id.tvDetailEmail);
        tvPhone = view.findViewById(R.id.tvDetailPhone);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Lấy mã user từ Activity cha
        if (getActivity() instanceof StudentHomeActivity) {
            currentUserCode = ((StudentHomeActivity) getActivity()).getMyMaSV();
        } else if (getActivity() instanceof TeacherHomeActivity) {
            currentUserCode = ((TeacherHomeActivity) getActivity()).getMyMaSV();
        }

        loadUserProfile();

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        return view;
    }

    private void loadUserProfile() {
        if (currentUserCode == null) return;

        // Gọi hàm getUserInfo đã sửa ở Bước 1
        JDBCService.getUserInfo(currentUserCode, user -> {
            if (user != null) {
                // SỬA CÁC DÒNG NÀY ĐỂ KHỚP VỚI UserInfoModel
                tvName.setText(user.fullName);
                tvID.setText("Mã: " + user.userCode);

                // Dùng .classInfo thay vì .className
                tvLop.setText("Lớp: " + (user.classInfo != null ? user.classInfo : "Trống"));
                tvKhoa.setText("Khóa: " + (user.course != null ? user.course : "Trống"));
                tvNganh.setText("Ngành: " + (user.major != null ? user.major : "Trống"));
                tvEmail.setText("Email: " + (user.email != null ? user.email : "Trống"));
                tvPhone.setText("SĐT: " + (user.phone != null ? user.phone : "Trống"));
            } else {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}