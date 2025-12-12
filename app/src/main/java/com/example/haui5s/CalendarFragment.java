package com.example.haui5s;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    CalendarView calendarView;
    TextView tvTaskInfo;
    FloatingActionButton fabAddSchedule;

    // Danh sách lưu dữ liệu tải từ DB về để so sánh khi click
    List<ScheduleModel> loadedSchedules = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        tvTaskInfo = view.findViewById(R.id.tvTaskInfo);
        fabAddSchedule = view.findViewById(R.id.fabAddSchedule);

        // 1. Tải dữ liệu ngay khi mở màn hình
        loadEventsFromDB();

        // 2. Xử lý sự kiện Click vào ngày
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDay = eventDay.getCalendar();
            showTaskInfo(clickedDay);
        });

        fabAddSchedule.setOnClickListener(v -> showAddScheduleDialog());

        return view;
    }

    // HÀM TẢI DỮ LIỆU TỪ DB VÀ TẠO CHẤM ĐỎ
    private void loadEventsFromDB() {
        JDBCService.getAllSchedules(schedules -> {
            loadedSchedules.clear();
            loadedSchedules.addAll(schedules);

            List<EventDay> events = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            for (ScheduleModel item : schedules) {
                try {
                    Calendar cal = Calendar.getInstance();
                    // Chuyển chuỗi ngày từ DB thành Calendar
                    cal.setTime(sdf.parse(item.scheduleDate));

                    // Thêm chấm đỏ vào lịch
                    events.add(new EventDay(cal, R.drawable.ic_dot));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Cập nhật lên giao diện
            calendarView.setEvents(events);
        });
    }

    // HÀM HIỂN THỊ THÔNG TIN KHI CLICK VÀO NGÀY
    private void showTaskInfo(Calendar clickedDay) {
        StringBuilder infoBuilder = new StringBuilder();
        boolean hasTask = false;

        for (ScheduleModel item : loadedSchedules) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Calendar itemCal = Calendar.getInstance();
                itemCal.setTime(sdf.parse(item.scheduleDate));

                // So sánh ngày click và ngày trong DB (bỏ qua giờ phút giây)
                if (isSameDay(clickedDay, itemCal)) {
                    hasTask = true;
                    // Format giờ hiển thị cho đẹp (HH:mm)
                    String timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(itemCal.getTime());

                    infoBuilder.append("⏰ ").append(timeStr).append("\n")
                            .append("👤 ").append(item.personName).append("\n")
                            .append("📍 ").append(item.area).append("\n")
                            .append("📝 ").append(item.note).append("\n\n-----------------\n\n");
                }
            } catch (Exception e) { }
        }

        if (hasTask) {
            tvTaskInfo.setText(infoBuilder.toString());
        } else {
            tvTaskInfo.setText("Không có lịch trực nhật nào trong ngày này.");
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void showAddScheduleDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_schedule, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText etCode = dialogView.findViewById(R.id.etSchStudentCode);
        EditText etName = dialogView.findViewById(R.id.etSchStudentName);
        EditText etClass = dialogView.findViewById(R.id.etSchClass);
        EditText etArea = dialogView.findViewById(R.id.etSchArea);
        EditText etNote = dialogView.findViewById(R.id.etSchNote);
        TextView tvDate = dialogView.findViewById(R.id.tvSchDate);
        TextView tvTime = dialogView.findViewById(R.id.tvSchTime);
        Button btnSave = dialogView.findViewById(R.id.btnSaveSchedule);

        // LOGIC TÌM KIẾM (Giữ nguyên)
        etCode.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etCode.getRight() - etCode.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    String inputMSV = etCode.getText().toString().trim();
                    if (!inputMSV.isEmpty()) {
                        Toast.makeText(getContext(), "Đang tìm...", Toast.LENGTH_SHORT).show();
                        JDBCService.getUserInfo(inputMSV, user -> {
                            if (user != null) {
                                etName.setText(user.fullName);
                                String lop = (user.classInfo != null) ? user.classInfo : "";
                                etClass.setText(lop);
                                Toast.makeText(getContext(), "Đã tìm thấy!", Toast.LENGTH_SHORT).show();
                            } else {
                                etName.setText(""); etClass.setText("");
                                Toast.makeText(getContext(), "Không tìm thấy!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    return true;
                }
            }
            return false;
        });

        // Chọn Ngày
        tvDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) ->
                    tvDate.setText(String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Chọn Giờ
        tvTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(getContext(), (view, hourOfDay, minute) ->
                    tvTime.setText(String.format("%02d:%02d", hourOfDay, minute)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        btnSave.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String lop = etClass.getText().toString().trim();
            String area = etArea.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            String sDate = tvDate.getText().toString().trim();
            String sTime = tvTime.getText().toString().trim();

            if (code.isEmpty() || name.isEmpty() || area.isEmpty() || sDate.isEmpty() || sTime.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin bắt buộc (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            String fullDateTime = sDate + " " + sTime;

            JDBCService.insertCleaningSchedule(code, name, lop, area, note, fullDateTime, success -> {
                if (success) {
                    Toast.makeText(getContext(), "Lưu thành công!", Toast.LENGTH_SHORT).show();
                    // QUAN TRỌNG: Tải lại dữ liệu để cập nhật chấm đỏ ngay lập tức
                    loadEventsFromDB();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Lỗi: Mã SV không tồn tại!", Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }
}