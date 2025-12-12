package com.example.haui5s;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private Context context;
    private List<ReportModel> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ReportModel item);
    }

    public ReportAdapter(Context context, List<ReportModel> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportModel item = list.get(position);

        // Hiển thị thông tin người làm vệ sinh
        holder.tvArea.setText("Khu vực: " + item.area);
        holder.tvReporter.setText("Người thực hiện: " + item.reporterName);
        holder.tvTime.setText("Thời gian: " + item.reportTime);
        holder.tvNote.setText("Ghi chú: " + item.note);

        // Xử lý trạng thái chấm điểm 5S
        if (item.status == 0) {
            holder.tvStatus.setText("Chờ chấm điểm");
            holder.tvStatus.setTextColor(Color.parseColor("#FFA500")); // Màu cam
            holder.tvScore.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setText("Đã chấm điểm");
            holder.tvStatus.setTextColor(Color.parseColor("#008000")); // Màu xanh lá
            holder.tvScore.setVisibility(View.VISIBLE);
            // Hiển thị điểm tổng kết
            holder.tvScore.setText("Điểm 5S: " + item.finalEvaluation + "/100");
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvArea, tvReporter, tvTime, tvStatus, tvScore, tvNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArea = itemView.findViewById(R.id.tv_item_area);
            tvReporter = itemView.findViewById(R.id.tv_item_reporter);
            tvTime = itemView.findViewById(R.id.tv_item_time);
            tvStatus = itemView.findViewById(R.id.tv_item_status);
            tvScore = itemView.findViewById(R.id.tv_item_score);
            tvNote = itemView.findViewById(R.id.tv_item_note);
        }
    }
}