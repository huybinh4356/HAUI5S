package com.example.haui5s;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BorrowAdapter extends RecyclerView.Adapter<BorrowAdapter.ViewHolder> {

    private Context context;
    private List<BorrowModel> borrowList;
    private boolean isTeacher; // Biến xác định quyền

    // Cập nhật Constructor thêm biến isTeacher
    public BorrowAdapter(Context context, List<BorrowModel> borrowList, boolean isTeacher) {
        this.context = context;
        this.borrowList = borrowList;
        this.isTeacher = isTeacher;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_borrow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BorrowModel item = borrowList.get(position);

        holder.tvStudentName.setText(item.getStudentName());
        holder.tvToolList.setText(item.getToolName());
        holder.tvBorrowDate.setText(item.getBorrowDate());

        String[] options = {"Chưa trả", "Đã trả"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, options);
        holder.spinnerStatus.setAdapter(adapterSpinner);

        // Set giá trị hiện tại
        holder.spinnerStatus.setSelection(item.getStatus() == 1 ? 1 : 0);

        // LOGIC PHÂN QUYỀN:
        if (isTeacher) {
            // Nếu là GV: Cho phép bấm, màu đậm
            holder.spinnerStatus.setEnabled(true);
            holder.spinnerStatus.setAlpha(1.0f);
        } else {
            // Nếu là SV: Khóa lại, làm mờ đi
            holder.spinnerStatus.setEnabled(false);
            holder.spinnerStatus.setAlpha(0.7f);
        }

        // Sự kiện chọn (Chỉ chạy nếu enabled = true)
        holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isTeacher) return; // Sinh viên không được update DB

                int newStatus = position;
                if (newStatus != item.getStatus()) {
                    JDBCService.updateBorrowStatus(item.getBorrowId(), newStatus, success -> {
                        if (success) {
                            item.setStatus(newStatus);
                            Toast.makeText(context, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                        } else {
                            holder.spinnerStatus.setSelection(item.getStatus()); // Reset nếu lỗi
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    @Override
    public int getItemCount() {
        return borrowList != null ? borrowList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvToolList, tvBorrowDate;
        Spinner spinnerStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvToolList = itemView.findViewById(R.id.tvToolList);
            tvBorrowDate = itemView.findViewById(R.id.tvBorrowDate);
            spinnerStatus = itemView.findViewById(R.id.spinnerStatus);
        }
    }
}