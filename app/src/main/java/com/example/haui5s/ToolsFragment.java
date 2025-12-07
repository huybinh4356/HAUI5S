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

        // KHÔNG GỌI JDBC NGAY Ở ĐÂY - CHỈ GỌI KHI CLICK VÀO NGÀY
        // loadDataForDate(selectedDateString);

        fabAdd.setOnClickListener(v -> showAddDialog());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Load data sau khi view đã được tạo xong
        if (selectedDateString != null) {
            loadDataForDate(selectedDateString);
        }
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
        weekCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dayOnlyFormat = new SimpleDateFormat("dd", Locale.getDefault());

        String todayStr = fullDateFormat.format(currentCalendar.getTime());
        selectedDateString = todayStr;

        for (int i = 0; i < 7; i++) {
            TextView tv = tvDays[i];

            String dateOfView = fullDateFormat.format(weekCal.getTime());
            String dayNumber = dayOnlyFormat.format(weekCal.getTime());
            String dayName = getDayName(i);

            tv.setText(dayName + "\n" + dayNumber);

            if (dateOfView.equals(selectedDateString)) {
                highlightDate(tv);
            } else {
                unhighlightDate(tv);
            }

            final String currentDate = dateOfView;
            tv.setOnClickListener(v -> {
                for (TextView t : tvDays) unhighlightDate(t);
                highlightDate((TextView) v);
                selectedDateString = currentDate;
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

    // --- LOAD DỮ LIỆU TỪ DB ---
    private void loadDataForDate(String date) {
        listBorrow.clear();
        adapter.notifyDataSetChanged();

        Log.d("TOOLS_FRAGMENT", "Đang load data cho ngày: " + date);

        // KIỂM TRA CONTEXT TRƯỚC KHI GỌI JDBC
        if (getContext() == null) {
            Log.e("TOOLS_FRAGMENT", "Context is null");
            return;
        }

        // TẠM THỜI DÙNG DATA MẪU (COMMENT JDBC LẠI)
        // Thêm data mẫu để test UI
        listBorrow.add(new BorrowModel(1, "Nguyễn Văn A", "Chổi (2), Giẻ lau (1)", "07/12/2024 08:00:00", 0));
        listBorrow.add(new BorrowModel(2, "Trần Thị B", "Chậu (1), Hót rác (1)", "07/12/2024 09:30:00", 1));
        adapter.notifyDataSetChanged();

        /*
        // --- GỌI JDBC SERVICE LẤY DANH SÁCH ---
        try {
            JDBCService.getBorrowListByDate(date, list -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        listBorrow.clear();
                        if (list != null) {
                            listBorrow.addAll(list);
                        }
                        adapter.notifyDataSetChanged();

                        if (list == null || list.isEmpty()) {
                            Toast.makeText(getContext(), "Không có phiếu mượn nào cho ngày " + date, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e("TOOLS_FRAGMENT", "Lỗi khi gọi JDBC: " + e.getMessage());
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi kết nối database", Toast.LENGTH_SHORT).show();
            }
        }
        */
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

            // TẠM THỜI KHÔNG GỌI JDBC - CHỈ THÊM VÀO LIST
            BorrowModel newBorrow = new BorrowModel(
                    listBorrow.size() + 1,
                    name,
                    finalToolList,
                    selectedDateString + " 10:00:00",
                    0
            );
            listBorrow.add(newBorrow);
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Đã thêm phiếu mượn (test)", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            /*
            // --- GỌI JDBC SERVICE LƯU PHIẾU MƯỢN ---
            JDBCService.insertBorrow(idSv, name, finalToolList, selectedDateString, 0, check -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (check) {
                            Toast.makeText(getContext(), "Đã lưu phiếu mượn", Toast.LENGTH_SHORT).show();
                            loadDataForDate(selectedDateString);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi lưu", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            */
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