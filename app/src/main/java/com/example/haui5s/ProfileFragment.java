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

        String masv = "";
        if(getActivity() instanceof StudentHomeActivity) {
            masv = ((StudentHomeActivity) getActivity()).getMyMaSV();
        } else if (getActivity() instanceof TeacherHomeActivity) {
            masv = ((TeacherHomeActivity) getActivity()).getMyMaSV();
        }

        // --- GỌI JDBC SERVICE LẤY THÔNG TIN USER ---
        String finalMasv = masv;
        JDBCService.getUserInfo(masv, user -> {
            if(user != null) {
                tvName.setText(user.fullName);
                tvID.setText("Mã SV: " + finalMasv);
                tvLop.setText("Lớp: " + user.classInfo + " - " + user.course);
                tvNganh.setText("Ngành: " + user.major);
                tvEmail.setText("Email: " + user.email);
            } else {
                Toast.makeText(getActivity(), "Không thể tải thông tin người dùng!", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }
}