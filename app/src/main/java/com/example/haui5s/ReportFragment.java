package com.example.haui5s;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.haui5s.JDBCService;
import com.example.haui5s.R;
import com.example.haui5s.StudentHomeActivity;
import com.example.haui5s.TeacherHomeActivity;
import com.example.haui5s.api.ImgBBService;
import com.example.haui5s.utils.DataUtils;
import com.example.haui5s.utils.FileUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.haui5s.api.ImgBBService.ImgBBResponse; // Đã sửa lỗi báo đỏ

public class ReportFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<ReportModel> reportList;
    private FloatingActionButton fabAdd;

    private boolean isTeacher = false;
    private String currentUserCode = "USER";

    // --- BIẾN ĐỂ XỬ LÝ ẢNH (Trong Dialog) ---
    private TextView tvImgStatusTemp;
    private ImageView ivPreviewImageTemp;
    private String selectedImageStr = ""; // Lưu URI ảnh đã chọn (String)

    // Khai báo biến cho Tìm kiếm và Lọc
    private EditText etSearch;
    private Spinner spinnerStatusFilter;

    private List<ReportModel> fullReportList; // Danh sách gốc không bị thay đổi
    private String currentSearchQuery = "";
    private int currentStatusFilter = -1; // -1: Tất cả, 0: Pending, 1: Completed

    // Bộ khởi chạy Gallery
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();

                    if (selectedUri != null) {
                        selectedImageStr = selectedUri.toString();

                        // 1. Load ảnh xem trước và hiển thị
                        if (ivPreviewImageTemp != null) {
                            ivPreviewImageTemp.setVisibility(View.VISIBLE);
                            Glide.with(this)
                                    .load(selectedUri)
                                    .into(ivPreviewImageTemp);
                        }

                        // 2. Ẩn dòng Trạng thái
                        if (tvImgStatusTemp != null) {
                            tvImgStatusTemp.setVisibility(View.GONE);
                        }

                        Toast.makeText(getContext(), "Đã chọn ảnh thành công! Sẵn sàng để gửi.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Không nhận được dữ liệu ảnh.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Đã hủy chọn ảnh.", Toast.LENGTH_SHORT).show();
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

        // ÁNH XẠ VIEW MỚI
        etSearch = view.findViewById(R.id.et_search);
        spinnerStatusFilter = view.findViewById(R.id.spinner_status_filter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportList = new ArrayList<>();
        fullReportList = new ArrayList<>(); // Đây là danh sách toàn bộ dữ liệu gốc

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

        // Thiết lập Lọc và Tìm kiếm
        setupFilters();
        setupSearchListener();
        return view;
    }

    // Hàm thiết lập Spinner Filter
    private void setupFilters() {
        // Chuẩn bị danh sách trạng thái
        String[] statuses = new String[]{"Tất cả", "🔴 Chưa chấm", "🟢 Đã chấm"};

        // Tạo ArrayAdapter cho Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                statuses
        );
        spinnerStatusFilter.setAdapter(adapter);

        // Xử lý sự kiện chọn item
        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // position 0: Tất cả (-1), 1: Chưa xử lý (0), 2: Đã chấm điểm (1)
                currentStatusFilter = position - 1;
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì nếu không chọn
            }
        });
    }

    // Hàm thiết lập Tìm kiếm
    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim().toLowerCase(Locale.getDefault());
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Hàm quan trọng: Áp dụng cả tìm kiếm và lọc
    private void applyFilters() {
        List<ReportModel> filteredList = new ArrayList<>();

        for (ReportModel item : fullReportList) {
            // 1. Lọc theo trạng thái
            boolean statusMatch = (currentStatusFilter == -1) || (item.status == currentStatusFilter);

            // 2. Tìm kiếm theo từ khóa
            boolean searchMatch = currentSearchQuery.isEmpty() ||
                    item.area.toLowerCase(Locale.getDefault()).contains(currentSearchQuery) ||
                    item.note.toLowerCase(Locale.getDefault()).contains(currentSearchQuery);

            if (statusMatch && searchMatch) {
                filteredList.add(item);
            }
        }

        // Cập nhật RecyclerView
        reportList.clear();
        reportList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    private void loadData() {
        JDBCService.getReportList(currentUserCode, true, list -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (list != null) {
                        // Lưu dữ liệu gốc vào danh sách đầy đủ
                        fullReportList.clear();
                        fullReportList.addAll(list);

                        // Áp dụng bộ lọc (và tìm kiếm) ngay sau khi tải dữ liệu
                        applyFilters();
                    }
                });
            }
        });
    }

    // Hàm mở Gallery được cách ly
    private void openGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); // <-- Intent ổn định nhất
        intent.setType("image/*");

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            imagePickerLauncher.launch(intent);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ứng dụng quản lý ảnh.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- DIALOG THÊM BÁO CÁO ---
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_report, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etArea = view.findViewById(R.id.etArea);
        EditText etNote = view.findViewById(R.id.etNote);
        Button btnUpload = view.findViewById(R.id.btnUploadImg);

        // Ánh xạ các View ảnh
        tvImgStatusTemp = view.findViewById(R.id.tvImgStatus);
        ivPreviewImageTemp = view.findViewById(R.id.ivPreviewImage);
        Button btnSubmit = view.findViewById(R.id.btnSubmitReport);

        // --- THIẾT LẬP TRẠNG THÁI BAN ĐẦU ---
        selectedImageStr = "";
        ivPreviewImageTemp.setVisibility(View.GONE);
        tvImgStatusTemp.setVisibility(View.VISIBLE);
        tvImgStatusTemp.setText("Chưa có ảnh được chọn.");

        // SỰ KIỆN MỞ GALLERY
        btnUpload.setOnClickListener(v -> {
            openGalleryIntent(); // <-- Gọi hàm mở Intent đã cách ly
        });

        // SỰ KIỆN GỬI BÁO CÁO
        btnSubmit.setOnClickListener(v -> {
            String area = etArea.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (area.isEmpty()) {
                Toast.makeText(getContext(), "Nhập khu vực!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageStr.isEmpty()) {
                // KHÔNG CÓ ẢNH: Lưu thẳng với URL mặc định
                insertReportWithUrl(area, note, "no_image", dialog);
            } else {
                // CÓ ẢNH: Bắt đầu quá trình Upload lên ImgBB
                Uri imageUri = Uri.parse(selectedImageStr);
                uploadToImgBB(imageUri, area, note, dialog);
            }
        });
        dialog.show();
    }

    // 2. DIALOG CHẤM ĐIỂM (Giữ nguyên)
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

    private void showDetailDialog(ReportModel item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("🔍 CHI TIẾT BÁO CÁO 5S");
        StringBuilder msgBuilder = new StringBuilder();
        String reportId = DataUtils.generateReportCode(item.id);
        msgBuilder.append("📍 KHU VỰC BÁO CÁO\n")
                .append("==========================\n")
                .append(item.area)
                .append("\n\n");
        msgBuilder.append("📝 MÔ TẢ CHI TIẾT (Ghi chú)\n")
                .append("==========================\n")
                .append(item.note.isEmpty() ? "Không có mô tả chi tiết." : item.note)
                .append("\n");
        msgBuilder.append("\n-------------------------------------------\n\n");
        msgBuilder.append("🆔 Mã Báo Cáo:\n")
                .append("   ▶ **").append(reportId).append("**\n\n");
        msgBuilder.append("👤 Người Báo Cáo:\n")
                .append("   ▶ ").append(item.reporterCode).append("\n\n");
        msgBuilder.append("⏱️ Thời Gian Báo Cáo:\n")
                .append("   ▶ ").append(item.timestamp).append("\n");
        msgBuilder.append("\n-------------------------------------------\n\n");
        String statusMsg = (item.status == 0)
                ? "🔴 CHƯA XỬ LÝ (Pending)"
                : "🟢 ĐÃ CHẤM ĐIỂM (Completed)";
        msgBuilder.append("📊 Trạng Thái Xử Lý:\n")
                .append("   ▶ **").append(statusMsg).append("**\n");
        if (item.status == 1) {
            msgBuilder.append("\n")
                    .append("🏆 KẾT QUẢ CHẤM ĐIỂM:\n")
                    .append("   ▶ Điểm Tổng: **").append(item.finalEvaluation).append("/100**\n")
                    .append("   ▶ Nhận Xét: ").append(item.resolutionNote);
        }
        msgBuilder.append("\n-------------------------------------------\n\n");
        msgBuilder.append("🖼️ Ảnh Minh Chứng:\n");
        if (item.imageUrl != null && !item.imageUrl.isEmpty() && !item.imageUrl.equals("no_image")) {
            msgBuilder.append("  * Ảnh đã được tải lên máy chủ\n")
                    .append("  * URL: ").append(item.imageUrl);
        } else {
            msgBuilder.append("  * Không có ảnh minh chứng đính kèm");
        }

        builder.setMessage(msgBuilder.toString());
        builder.setPositiveButton("Đóng", (d, w) -> d.dismiss());
        builder.show();
    }

    private void uploadToImgBB(Uri imageUri, String area, String note, AlertDialog dialog) {
        // 1. Lấy File từ URI (Hàm này phức tạp, cần viết hàm phụ trợ: getRealPathFromURI)
        File file = FileUtils.getFile(getContext(), imageUri); // Giả định bạn có một FileUtils.getFile

        if (file == null) {
            Toast.makeText(getContext(), "Không thể đọc file ảnh.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạm thời hiển thị "Đang tải lên..."
        tvImgStatusTemp.setVisibility(View.VISIBLE);
        tvImgStatusTemp.setText("⏳ Đang tải ảnh lên ImgBB...");
        ivPreviewImageTemp.setVisibility(View.GONE);

        // 2. Tạo Retrofit instance và gọi API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.imgbb.com/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ImgBBService service = retrofit.create(ImgBBService.class);

        // Tạo Request Body
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        String KEY = "c75a48c8fab66f61d7d8a9ad98b4a90f";
        Call<ImgBBResponse> call = service.uploadImage(KEY, imagePart);

        call.enqueue(new Callback<ImgBBResponse>() {
            @Override
            public void onResponse(Call<ImgBBResponse> call, Response<ImgBBResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    // Tải lên thành công!
                    String publicUrl = response.body().data.url;

                    // 3. Lưu vào DB (Gọi hàm insertReport)
                    insertReportWithUrl(area, note, publicUrl, dialog);
                } else {
                    Toast.makeText(getContext(), "Lỗi ImgBB: " + response.code() + " - " + response.message(), Toast.LENGTH_LONG).show();
                    tvImgStatusTemp.setText("❌ Tải ảnh thất bại.");
                    ivPreviewImageTemp.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ImgBBResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                tvImgStatusTemp.setText("❌ Lỗi mạng khi tải ảnh.");
                ivPreviewImageTemp.setVisibility(View.VISIBLE);
            }
        });
    }

    // Hàm gửi báo cáo sau khi có URL công khai
    private void insertReportWithUrl(String area, String note, String imageUrl, AlertDialog dialog) {
        // Gọi dịch vụ JDBC của bạn với URL công khai

        JDBCService.insertReport(currentUserCode, currentUserCode, area, note, imageUrl, success -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), "Gửi báo cáo thành công!", Toast.LENGTH_SHORT).show();
                        loadData();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Lỗi: Lưu DB thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}