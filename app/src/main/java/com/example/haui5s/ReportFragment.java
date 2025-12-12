package com.example.haui5s;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ReportFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<ReportModel> reportList;
    private FloatingActionButton fabAdd;

    private boolean isTeacher = false;
    private String currentUserCode = "USER";

    // --- BIẾN ĐỂ XỬ LÝ ẢNH ---
    private TextView tvImgStatusTemp; // Biến tạm để cập nhật giao diện Dialog
    private String selectedImageStr = ""; // Lưu đường dẫn ảnh để gửi lên DB

    // Bộ khởi chạy Gallery (Phải khai báo trước onCreate)
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        selectedImageStr = selectedUri.toString(); // Lưu URI ảnh
                        // Cập nhật giao diện Dialog
                        if (tvImgStatusTemp != null) {
                            tvImgStatusTemp.setText("Đã chọn ảnh thành công!");
                            tvImgStatusTemp.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        }
                    }
                }
            }
    );

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TeacherHomeActivity) {
            isTeacher = true;
            currentUserCode = ((TeacherHomeActivity) context).getMyMaSV();
        } else if (context instanceof StudentHomeActivity) {
            isTeacher = false;
            currentUserCode = ((StudentHomeActivity) context).getMyMaSV();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        recyclerView = view.findViewById(R.id.recycler_report);
        fabAdd = view.findViewById(R.id.fabAddReport);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportList = new ArrayList<>();

        adapter = new ReportAdapter(getContext(), reportList, item -> {
            if (isTeacher) {
                showGradingDialog(item);
            } else {
                showDetailDialog(item);
            }
        });
        recyclerView.setAdapter(adapter);

        fabAdd.setVisibility(View.VISIBLE);
        fabAdd.setOnClickListener(v -> showAddDialog());

        loadData();
        return view;
    }

    private void loadData() {
        JDBCService.getReportList(currentUserCode, true, list -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (list != null) {
                        reportList.clear();
                        reportList.addAll(list);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    // --- 1. DIALOG THÊM BÁO CÁO (ĐÃ SỬA NÚT ẢNH) ---
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_report, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etArea = view.findViewById(R.id.etArea);
        EditText etNote = view.findViewById(R.id.etNote);
        Button btnUpload = view.findViewById(R.id.btnUploadImg);
        tvImgStatusTemp = view.findViewById(R.id.tvImgStatus); // Gán vào biến toàn cục để update sau
        Button btnSubmit = view.findViewById(R.id.btnSubmitReport);

        // Reset biến ảnh
        selectedImageStr = "";

        // SỰ KIỆN MỞ GALLERY THẬT
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> {
            String area = etArea.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (area.isEmpty()) {
                Toast.makeText(getContext(), "Nhập khu vực!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Nếu chưa chọn ảnh thì dùng ảnh mặc định
            if (selectedImageStr.isEmpty()) {
                selectedImageStr = "no_image";
            }

            JDBCService.insertReport(currentUserCode, currentUserCode, area, note, selectedImageStr, success -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(getContext(), "Gửi báo cáo thành công!", Toast.LENGTH_SHORT).show();
                            loadData();
                            dialog.dismiss();
                        }
                    });
                }
            });
        });
        dialog.show();
    }

    // 2. DIALOG CHẤM ĐIỂM
    private void showGradingDialog(ReportModel item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chấm điểm: " + item.area);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText s1 = new EditText(getContext()); s1.setHint("S1"); s1.setInputType(2); layout.addView(s1);
        final EditText s2 = new EditText(getContext()); s2.setHint("S2"); s2.setInputType(2); layout.addView(s2);
        final EditText s3 = new EditText(getContext()); s3.setHint("S3"); s3.setInputType(2); layout.addView(s3);
        final EditText s4 = new EditText(getContext()); s4.setHint("S4"); s4.setInputType(2); layout.addView(s4);
        final EditText s5 = new EditText(getContext()); s5.setHint("S5"); s5.setInputType(2); layout.addView(s5);
        final EditText note = new EditText(getContext()); note.setHint("Nhận xét"); layout.addView(note);

        if (item.status == 1) {
            s1.setText(String.valueOf(item.scoreS1));
            s2.setText(String.valueOf(item.scoreS2));
            s3.setText(String.valueOf(item.scoreS3));
            s4.setText(String.valueOf(item.scoreS4));
            s5.setText(String.valueOf(item.scoreS5));
            note.setText(item.resolutionNote);
        }

        builder.setView(layout);
        builder.setPositiveButton("LƯU", (d, w) -> {
            try {
                int sc1 = Integer.parseInt(s1.getText().toString());
                int sc2 = Integer.parseInt(s2.getText().toString());
                int sc3 = Integer.parseInt(s3.getText().toString());
                int sc4 = Integer.parseInt(s4.getText().toString());
                int sc5 = Integer.parseInt(s5.getText().toString());
                String cmt = note.getText().toString();

                JDBCService.updateReportStatus(item.id, currentUserCode, cmt, sc1, sc2, sc3, sc4, sc5, success -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Đã chấm điểm!", Toast.LENGTH_SHORT).show();
                            loadData();
                        });
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getContext(), "Nhập số!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    // 3. DIALOG XEM CHI TIẾT
    private void showDetailDialog(ReportModel item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chi tiết 5S: " + item.area);

        String statusMsg = (item.status == 0) ? "Chưa xử lý" : "Đã chấm điểm";
        String msg = "Người báo: " + item.reporterName + "\n" +
                "Mô tả: " + item.note + "\n\n" +
                "Trạng thái: " + statusMsg + "\n";

        if (item.status == 1) {
            msg += "----------------\n" +
                    "Điểm: " + item.finalEvaluation + "/100\n" +
                    "Nhận xét: " + item.resolutionNote;
        }

        builder.setMessage(msg);
        builder.setPositiveButton("Đóng", (d, w) -> d.dismiss());
        builder.show();
    }
}