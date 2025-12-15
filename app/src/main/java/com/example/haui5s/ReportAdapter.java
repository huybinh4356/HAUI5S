package com.example.haui5s;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private Context context;
    private List<ReportModel> list;
    private OnItemClickListener listener;

    // Interface để xử lý sự kiện click
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
        // Đảm bảo bạn đã có file layout tên là item_report.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportModel item = list.get(position);

        // Hiển thị thông tin cơ bản
        holder.tvArea.setText("Khu vực: " + item.area);
        holder.tvReporter.setText("Người thực hiện: " + item.reporterName);
        holder.tvNote.setText("Ghi chú: " + item.note);

        // --- SỬA LỖI Ở ĐÂY: Dùng item.timestamp (khớp với Model) ---
        holder.tvTime.setText("Thời gian: " + item.timestamp);

        // Xử lý trạng thái chấm điểm 5S
        if (item.status == 0) {
            // Trạng thái: Chờ chấm
            holder.tvStatus.setText("Chờ chấm điểm");
            holder.tvStatus.setTextColor(Color.parseColor("#FFA500")); // Màu cam
            holder.tvScore.setVisibility(View.GONE); // Ẩn điểm đi
        } else {
            // Trạng thái: Đã chấm
            holder.tvStatus.setText("Đã chấm điểm");
            holder.tvStatus.setTextColor(Color.parseColor("#008000")); // Màu xanh lá
            holder.tvScore.setVisibility(View.VISIBLE); // Hiện điểm
            // Hiển thị điểm tổng kết
            holder.tvScore.setText("Điểm 5S: " + item.finalEvaluation + "/100");
        }

        // LOGIC MỚI: HIỂN THỊ ẢNH TỪ URL
        if (item.imageUrl != null && !item.imageUrl.equals("no_image")) {
            holder.ivImage.setVisibility(View.VISIBLE);
            // Tải ảnh từ URL công khai (ImgBB)
            Glide.with(context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder) // Bạn nên có icon placeholder
                    .error(R.drawable.ic_image_error) // Icon báo lỗi
                    .into(holder.ivImage);
        } else {
            // Ẩn ImageView nếu không có ảnh
            holder.ivImage.setVisibility(View.GONE);
        }

        // Bắt sự kiện click vào item (để xem chi tiết hoặc chấm điểm)
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvArea, tvReporter, tvTime, tvStatus, tvScore, tvNote;
        ImageView ivImage; //

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ View (Đảm bảo ID trong item_report.xml đúng như này)
            tvArea = itemView.findViewById(R.id.tv_item_area);
            tvReporter = itemView.findViewById(R.id.tv_item_reporter);
            tvTime = itemView.findViewById(R.id.tv_item_time);
            tvStatus = itemView.findViewById(R.id.tv_item_status);
            tvScore = itemView.findViewById(R.id.tv_item_score);
            tvNote = itemView.findViewById(R.id.tv_item_note);
            ivImage = itemView.findViewById(R.id.iv_item_image);
        }
    }
}