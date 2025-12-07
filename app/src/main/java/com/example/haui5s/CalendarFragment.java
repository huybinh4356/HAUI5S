package com.example.haui5s;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    CalendarView calendarView;
    TextView tvTaskInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Trỏ vào file XML bạn vừa gửi
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Ánh xạ đúng ID trong XML của bạn
        calendarView = view.findViewById(R.id.calendarView);
        tvTaskInfo = view.findViewById(R.id.tvTaskInfo);

        // --- TẠO DẤU CHẤM (EVENT) ---
        List<EventDay> events = new ArrayList<>();

        // Ví dụ: Tạo dấu chấm vào ngày mai
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.DAY_OF_MONTH, 1);
        // R.drawable.ic_dot là file hình tròn đỏ bạn đã tạo ở bước trước
        events.add(new EventDay(calendar1, R.drawable.ic_dot));

        // Ví dụ: Tạo dấu chấm vào 5 ngày sau
        Calendar calendar2 = Calendar.getInstance();
        calendar2.add(Calendar.DAY_OF_MONTH, 5);
        events.add(new EventDay(calendar2, R.drawable.ic_dot));

        // Đẩy sự kiện lên lịch
        calendarView.setEvents(events);

        // --- BẮT SỰ KIỆN CHỌN NGÀY ---
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDay = eventDay.getCalendar();

            // Kiểm tra ngày được chọn có trùng với ngày có lịch không
            if (isSameDay(clickedDay, calendar1)) {
                tvTaskInfo.setText("Lịch trình: Trực nhật Khu A (07:00 - 11:00)");
            } else if (isSameDay(clickedDay, calendar2)) {
                tvTaskInfo.setText("Lịch trình: Kiểm tra vệ sinh (14:00 - 16:00)");
            } else {
                tvTaskInfo.setText("Không có lịch trình cho ngày này.");
            }
        });

        return view;
    }

    // Hàm so sánh 2 ngày có giống nhau không
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
}