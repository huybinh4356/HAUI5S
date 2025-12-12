package com.example.haui5s;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ToolsFragment extends Fragment {

    private RecyclerView rvBorrowList;
    private FloatingActionButton fabAdd;
    private TextView[] tvDays = new TextView[7];

    private List<BorrowModel> listBorrow;
    private BorrowAdapter adapter;

    private Calendar currentCalendar;
    private String selectedDateString;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tools, container, false);

        initViews(view);
        initData();

        setupRecyclerView();
        setupCalendarLogic();

        // Load dữ liệu ngay khi mở màn hình
        loadDataForDate(selectedDateString);

        fabAdd.setOnClickListener(v -> showAddDialog());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initViews(View view) {
        rvBorrowList = view.findViewById(R.id.rvBorrowList);
        fabAdd = view.findViewById(R.id.fabAdd);

        tvDays[0] = view.findViewById(R.id.tvT2);
        tvDays[1] = view.findViewById(R.id.tvT3);
        tvDays[2] = view.findViewById(R.id.tvT4);
        tvDays[3] = view.findViewById(R.id.tvT5);
        tvDays[4] = view.findViewById(R.id.tvT6);
        tvDays[5] = view.findViewById(R.id.tvT7);
        tvDays[6] = view.findViewById(R.id.tvCN);
    }

    private void initData() {
        listBorrow = new ArrayList<>();
        currentCalendar = Calendar.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new BorrowAdapter(getContext(), listBorrow);
        rvBorrowList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBorrowList.setAdapter(adapter);
    }

    private void setupCalendarLogic() {
        Calendar weekCal = (Calendar) currentCalendar.clone();
        // Set về đầu tuần (Thứ 2) để hiển thị đúng dải
        // Lưu ý: Tùy locale, ở VN FirstDayOfWeek thường là Monday
        weekCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // Nếu hôm nay là Chủ Nhật, logic trên có thể bị lùi về tuần trước
        // (Fix nhanh: Nếu muốn luôn hiển thị tuần hiện tại chứa ngày hôm nay)
        // Nhưng tạm thời giữ logic đơn giản của bạn

        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Định dạng chuẩn SQL
        SimpleDateFormat dayOnlyFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        // Lấy ngày hiện tại format theo SQL để query
        selectedDateString = fullDateFormat.format(currentCalendar.getTime());

        for (int i = 0; i < 7; i++) {
            TextView tv = tvDays[i];

            // Ngày dùng để hiển thị trên UI
            String dayNumber = dayOnlyFormat.format(weekCal.getTime());
            // Ngày dùng để Query DB (yyyy-MM-dd)
            String queryDate = fullDateFormat.format(weekCal.getTime());

            String dayName = getDayName(i);
            tv.setText(dayName + "\n" + dayNumber);

            // So sánh ngày để highlight
            if (queryDate.equals(selectedDateString)) {
                highlightDate(tv);
            } else {
                unhighlightDate(tv);
            }

            // Sự kiện click vào ngày
            tv.setOnClickListener(v -> {
                for (TextView t : tvDays) unhighlightDate(t);
                highlightDate((TextView) v);
                selectedDateString = queryDate; // Cập nhật ngày được chọn
                loadDataForDate(selectedDateString);
            });

            weekCal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private String getDayName(int index) {
        switch (index) {
            case 0: return "T2";
            case 1: return "T3";
            case 2: return "T4";
            case 3: return "T5";
            case 4: return "T6";
            case 5: return "T7";
            case 6: return "CN";
            default: return "";
        }
    }

    private void highlightDate(TextView tv) {
        tv.setBackgroundColor(Color.parseColor("#00BFFF"));
        tv.setTextColor(Color.WHITE);
    }

    private void unhighlightDate(TextView tv) {
        tv.setBackgroundColor(Color.TRANSPARENT);
        tv.setTextColor(Color.BLACK);
    }

    // --- LOAD DỮ LIỆU TỪ DB (Đã sửa lại) ---
    private void loadDataForDate(String date) {
        if (getContext() == null) return;

        Log.d("TOOLS_FRAGMENT", "Querying date: " + date);

        // Gọi JDBC Service lấy danh sách mượn theo ngày
        JDBCService.getBorrowListByDate(date, list -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    listBorrow.clear();
                    if (list != null && !list.isEmpty()) {
                        listBorrow.addAll(list);
                    }
                    adapter.notifyDataSetChanged();

                    // Log để kiểm tra
                    Log.d("TOOLS_FRAGMENT", "Loaded " + listBorrow.size() + " items.");
                });
            }
        });
    }

    private void showAddDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.layout_add_borrow, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Ánh xạ View
        EditText etId = dialogView.findViewById(R.id.etStudentId);
        EditText etName = dialogView.findViewById(R.id.etStudentName);
        EditText etQtyChoi = dialogView.findViewById(R.id.etQtyChoi);
        EditText etQtyGie = dialogView.findViewById(R.id.etQtyGie);
        EditText etQtyChau = dialogView.findViewById(R.id.etQtyChau);
        EditText etQtyLau = dialogView.findViewById(R.id.etQtyLauNha);
        EditText etQtyHot = dialogView.findViewById(R.id.etQtyHotRac);
        EditText etQtyThung = dialogView.findViewById(R.id.etQtyThungRac);
        EditText etOtherName = dialogView.findViewById(R.id.etOtherName);
        EditText etQtyOther = dialogView.findViewById(R.id.etQtyOther);
        Button btnSave = dialogView.findViewById(R.id.btnSaveBorrow);

        btnSave.setOnClickListener(v -> {
            String idSv = etId.getText().toString().trim();
            String name = etName.getText().toString().trim();

            if (idSv.isEmpty() || name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập Mã SV và Tên SV", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder toolString = new StringBuilder();
            appendToolIfQty(toolString, "Chổi", etQtyChoi);
            appendToolIfQty(toolString, "Giẻ lau", etQtyGie);
            appendToolIfQty(toolString, "Chậu", etQtyChau);
            appendToolIfQty(toolString, "Cây lau nhà", etQtyLau);
            appendToolIfQty(toolString, "Hót rác", etQtyHot);
            appendToolIfQty(toolString, "Thùng rác", etQtyThung);

            String otherName = etOtherName.getText().toString().trim();
            String otherQty = etQtyOther.getText().toString().trim();
            if(!otherName.isEmpty() && !otherQty.isEmpty()){
                if (toolString.length() > 0) toolString.append(", ");
                toolString.append(otherName).append(" (").append(otherQty).append(")");
            }

            String finalToolList = toolString.toString();

            if (finalToolList.isEmpty()) {
                Toast.makeText(getContext(), "Bạn chưa nhập số lượng dụng cụ nào!", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- CẬP NHẬT: THÊM idSv VÀO CONSTRUCTOR ---
            // Đây là chỗ sửa lỗi "Expected 6 arguments"
            // Hiển thị tạm thời lên list để người dùng thấy ngay
            listBorrow.add(new BorrowModel(
                    listBorrow.size() + 1,
                    idSv,   // <--- THÊM BIẾN NÀY VÀO
                    name,
                    finalToolList,
                    selectedDateString + " 10:00:00",
                    0
            ));
            adapter.notifyDataSetChanged();

            // --- GỌI JDBC ĐỂ LƯU VÀO DATABASE THẬT ---
            // Thời gian mượn lấy tạm thời gian hiện tại
            String borrowTime = selectedDateString + " " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

            JDBCService.insertBorrow(idSv, name, finalToolList, borrowTime, 0, check -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (check) {
                            Toast.makeText(getContext(), "Đã lưu vào CSDL", Toast.LENGTH_SHORT).show();
                            // Load lại dữ liệu chuẩn từ DB
                            loadDataForDate(selectedDateString);
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi lưu DB (Kiểm tra xem Mã SV có tồn tại không)", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

            dialog.dismiss();
        });

        dialog.show();
    }

    private void appendToolIfQty(StringBuilder sb, String toolName, EditText etQty) {
        String qtyStr = etQty.getText().toString().trim();
        if (!qtyStr.isEmpty()) {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(toolName).append(" (").append(qty).append(")");
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }
}