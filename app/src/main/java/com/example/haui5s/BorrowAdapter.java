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

    public BorrowAdapter(Context context, List<BorrowModel> borrowList) {
        this.context = context;
        this.borrowList = borrowList;
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

        // Hiển thị thông tin (Có thể hiển thị thêm Mã SV nếu muốn: item.getStudentCode())
        holder.tvStudentName.setText(item.getStudentName());
        holder.tvToolList.setText(item.getToolName()); // Đã sửa thành getToolName
        holder.tvBorrowDate.setText(item.getBorrowDate());

        // Cấu hình Spinner
        String[] options = {"Chưa trả", "Đã trả"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, options);
        holder.spinnerStatus.setAdapter(adapterSpinner);

        // Set trạng thái hiện tại
        // Giả định: 0 là Chưa trả, 1 là Đã trả (Khớp với TINYINT mặc định 0)
        if (item.getStatus() == 1) {
            holder.spinnerStatus.setSelection(1);
        } else {
            holder.spinnerStatus.setSelection(0);
        }

        // Xử lý sự kiện chọn Spinner
        holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newStatus = position;

                // Chỉ update nếu trạng thái thay đổi so với ban đầu
                if (newStatus != item.getStatus()) {
                    JDBCService.updateBorrowStatus(item.getBorrowId(), newStatus, success -> {
                        if (success) {
                            item.setStatus(newStatus); // Cập nhật lại Model để không bị nhảy lại
                            Toast.makeText(context, "Đã cập nhật: " + (newStatus == 1 ? "Đã trả" : "Chưa trả"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show();
                            // Reset spinner về cũ nếu lỗi
                            holder.spinnerStatus.setSelection(item.getStatus());
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