package com.example.haui5s;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ReportFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<ReportModel> reportList;

    // Giả lập mã giáo viên (cần thay bằng logic lấy user thật)
    private String currentTeacherCode = "GV_TEST";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        recyclerView = view.findViewById(R.id.recycler_report);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        reportList = new ArrayList<>();

        // Setup Adapter
        adapter = new ReportAdapter(getContext(), reportList, item -> {
            // Sự kiện click vào item
            if (item.status == 0) {
                showGradingDialog(item); // Hiện bảng chấm điểm 5S
            } else {
                Toast.makeText(getContext(), "Đã chấm: " + item.finalEvaluation + " điểm", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);
        loadData();

        return view;
    }

    private void loadData() {
        // Gọi JDBC lấy danh sách (isTeacher = true để lấy hết)
        JDBCService.getReportList(currentTeacherCode, true, list -> {
            if (list != null) {
                reportList.clear();
                reportList.addAll(list);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                }
            }
        });
    }

    // Dialog chấm điểm 5S
    private void showGradingDialog(ReportModel item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chấm điểm 5S - " + item.area);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        final EditText inputS1 = new EditText(getContext()); inputS1.setHint("Điểm S1 (Sàng lọc)"); inputS1.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); layout.addView(inputS1);
        final EditText inputS2 = new EditText(getContext()); inputS2.setHint("Điểm S2 (Sắp xếp)"); inputS2.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); layout.addView(inputS2);
        final EditText inputS3 = new EditText(getContext()); inputS3.setHint("Điểm S3 (Sạch sẽ)"); inputS3.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); layout.addView(inputS3);
        final EditText inputS4 = new EditText(getContext()); inputS4.setHint("Điểm S4 (Săn sóc)"); inputS4.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); layout.addView(inputS4);
        final EditText inputS5 = new EditText(getContext()); inputS5.setHint("Điểm S5 (Sẵn sàng)"); inputS5.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); layout.addView(inputS5);

        final EditText inputNote = new EditText(getContext()); inputNote.setHint("Nhận xét/Góp ý"); layout.addView(inputNote);

        builder.setView(layout);

        builder.setPositiveButton("Lưu điểm", (dialog, which) -> {
            try {
                int s1 = Integer.parseInt(inputS1.getText().toString());
                int s2 = Integer.parseInt(inputS2.getText().toString());
                int s3 = Integer.parseInt(inputS3.getText().toString());
                int s4 = Integer.parseInt(inputS4.getText().toString());
                int s5 = Integer.parseInt(inputS5.getText().toString());
                String note = inputNote.getText().toString();

                // Cập nhật xuống DB
                JDBCService.updateReportStatus(item.id, currentTeacherCode, note, s1, s2, s3, s4, s5, success -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(getContext(), "Chấm điểm thành công!", Toast.LENGTH_SHORT).show();
                                loadData(); // Load lại để thấy điểm mới
                            } else {
                                Toast.makeText(getContext(), "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            } catch (Exception e) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ điểm số!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}