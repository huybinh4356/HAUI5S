package com.example.haui5s;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

        // KIỂM TRA VÀ SỬA ID NẾU CẦN
        holder.tvStudentName.setText(item.getStudentName());
        holder.tvToolList.setText(item.getToolName());
        holder.tvBorrowDate.setText(item.getBorrowDate());

        String statusText;
        int statusColor;
        if (item.getStatus() == 1) {
            statusText = "Đã trả";
            statusColor = android.graphics.Color.GREEN;
        } else {
            statusText = "Chưa trả";
            statusColor = android.graphics.Color.RED;
        }
        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return borrowList != null ? borrowList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvToolList, tvBorrowDate, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // KIỂM TRA VÀ SỬA ID CHO KHỚP VỚI LAYOUT
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvToolList = itemView.findViewById(R.id.tvToolList);      // Kiểm tra ID này
            tvBorrowDate = itemView.findViewById(R.id.tvBorrowDate);  // Kiểm tra ID này
            tvStatus = itemView.findViewById(R.id.tvStatus);          // Kiểm tra ID này

            // Nếu ID khác, sửa lại cho đúng:
            // tvToolList = itemView.findViewById(R.id.tv_tool_list);
            // tvBorrowDate = itemView.findViewById(R.id.tv_borrow_date);
            // tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}