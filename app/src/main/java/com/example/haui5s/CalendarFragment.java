package com.example.haui5s;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvTaskInfo;
    private FloatingActionButton fabAdd;
    private boolean isTeacher = false;
    private String currentUserCode = "";
    private List<EventDay> events = new ArrayList<>();
    private List<ScheduleModel> scheduleList = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        initViews(view);
        loadSchedulesFromDB();
        return view;
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        tvTaskInfo = view.findViewById(R.id.tvTaskInfo);
        fabAdd = view.findViewById(R.id.fabAddSchedule);

        if (isTeacher) {
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> showAddDialog());
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDay = eventDay.getCalendar();
            showTaskDetails(clickedDay);
        });
    }

    private void loadSchedulesFromDB() {
        JDBCService.getSchedulesByRole(currentUserCode, isTeacher, list -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    scheduleList.clear();
                    events.clear();
                    if (list != null) {
                        scheduleList.addAll(list);
                        for (ScheduleModel item : list) {
                            try {
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                calendar.setTime(sdf.parse(item.getScheduleDate()));
                                events.add(new EventDay(calendar, R.drawable.ic_launcher_background));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        calendarView.setEvents(events);
                    }
                });
            }
        });
    }

    private void showTaskDetails(Calendar clickedDay) {
        StringBuilder info = new StringBuilder();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String clickedDateStr = sdfDate.format(clickedDay.getTime());
        boolean hasTask = false;

        for (ScheduleModel item : scheduleList) {
            if (item.getScheduleDate().startsWith(clickedDateStr)) {
                info.append("- Người trực: ").append(item.getPersonName()).append("\n")
                        .append("  Khu vực: ").append(item.getArea()).append("\n")
                        .append("  Ghi chú: ").append(item.getNote()).append("\n\n");
                hasTask = true;
            }
        }
        if (hasTask) {
            tvTaskInfo.setText(info.toString());
            tvTaskInfo.setTextColor(Color.BLACK);
        } else {
            tvTaskInfo.setText("Ngày này không có lịch trực.");
            tvTaskInfo.setTextColor(Color.GRAY);
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        final EditText etMaSV = new EditText(getContext()); etMaSV.setHint("Mã SV trực"); layout.addView(etMaSV);
        final EditText etTenSV = new EditText(getContext()); etTenSV.setHint("Tên SV"); layout.addView(etTenSV);
        final EditText etKhuVuc = new EditText(getContext()); etKhuVuc.setHint("Khu vực (Phòng, Hành lang...)"); layout.addView(etKhuVuc);
        final EditText etGhiChu = new EditText(getContext()); etGhiChu.setHint("Nội dung công việc"); layout.addView(etGhiChu);

        final TextView tvChonGio = new TextView(getContext());
        tvChonGio.setText("Chọn ngày giờ: Chạm để chọn");
        tvChonGio.setPadding(0, 30, 0, 30);
        tvChonGio.setTextSize(16);
        tvChonGio.setTextColor(Color.BLUE);
        layout.addView(tvChonGio);

        final Calendar selectedCal = Calendar.getInstance();
        tvChonGio.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                selectedCal.set(Calendar.YEAR, year);
                selectedCal.set(Calendar.MONTH, month);
                selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                    selectedCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCal.set(Calendar.MINUTE, minute);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    tvChonGio.setText("Đã chọn: " + sdf.format(selectedCal.getTime()));
                }, 8, 0, true).show();
            }, selectedCal.get(Calendar.YEAR), selectedCal.get(Calendar.MONTH), selectedCal.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(layout);
        builder.setTitle("Thêm lịch trực nhật");
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String ma = etMaSV.getText().toString();
            String ten = etTenSV.getText().toString();
            String khuVuc = etKhuVuc.getText().toString();
            String ghiChu = etGhiChu.getText().toString();
            SimpleDateFormat sqlFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String timeStr = sqlFormat.format(selectedCal.getTime());

            if(ma.isEmpty() || ten.isEmpty() || khuVuc.isEmpty()) {
                Toast.makeText(getContext(), "Nhập thiếu thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            JDBCService.insertCleaningSchedule(ma, ten, "Lớp", khuVuc, ghiChu, timeStr, success -> {
                if(getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if(success) {
                            Toast.makeText(getContext(), "Thêm lịch thành công!", Toast.LENGTH_SHORT).show();
                            loadSchedulesFromDB();
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi thêm!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}