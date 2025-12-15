package com.example.haui5s;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListPopupWindow;
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
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_schedule, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etMaSV = dialogView.findViewById(R.id.etDialogMaSV);
        EditText etTenSV = dialogView.findViewById(R.id.etDialogTenSV);
        EditText etLop = dialogView.findViewById(R.id.etDialogLop);
        EditText etKhuVuc = dialogView.findViewById(R.id.etDialogKhuVuc);
        EditText etGhiChu = dialogView.findViewById(R.id.etDialogGhiChu);
        TextView tvChonNgay = dialogView.findViewById(R.id.tvDialogChonNgay);
        TextView tvChonGio = dialogView.findViewById(R.id.tvDialogChonGio);
        Button btnLuu = dialogView.findViewById(R.id.btnDialogLuu);

        ListPopupWindow listPopupWindow = new ListPopupWindow(getContext());
        listPopupWindow.setAnchorView(etMaSV);
        listPopupWindow.setModal(false);

        final List<UserInfoModel> foundUsers = new ArrayList<>();

        etMaSV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (keyword.length() >= 2) {
                    JDBCService.searchUsers(keyword, list -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                foundUsers.clear();
                                foundUsers.addAll(list);

                                List<String> displayList = new ArrayList<>();
                                for (UserInfoModel u : list) {
                                    displayList.add(u.userCode + " - " + u.fullName);
                                }

                                if (!displayList.isEmpty()) {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                            getContext(),
                                            android.R.layout.simple_list_item_1,
                                            displayList
                                    );
                                    listPopupWindow.setAdapter(adapter);
                                    listPopupWindow.show();
                                } else {
                                    listPopupWindow.dismiss();
                                }
                            });
                        }
                    });
                } else {
                    listPopupWindow.dismiss();
                }
            }
        });

        listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            UserInfoModel selectedUser = foundUsers.get(position);
            etMaSV.setText(selectedUser.userCode);
            etTenSV.setText(selectedUser.fullName);
            etLop.setText(selectedUser.classInfo);
            etMaSV.setSelection(etMaSV.getText().length());
            listPopupWindow.dismiss();
        });

        final Calendar selectedCal = Calendar.getInstance();

        if (tvChonNgay != null) {
            tvChonNgay.setOnClickListener(v -> {
                new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                    selectedCal.set(Calendar.YEAR, year);
                    selectedCal.set(Calendar.MONTH, month);
                    selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    tvChonNgay.setText(sdf.format(selectedCal.getTime()));
                }, selectedCal.get(Calendar.YEAR), selectedCal.get(Calendar.MONTH), selectedCal.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        if (tvChonGio != null) {
            tvChonGio.setOnClickListener(v -> {
                new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                    selectedCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCal.set(Calendar.MINUTE, minute);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    tvChonGio.setText(sdf.format(selectedCal.getTime()));
                }, 8, 0, true).show();
            });
        }

        if (btnLuu != null) {
            btnLuu.setOnClickListener(v -> {
                String ma = etMaSV.getText().toString().trim();
                String ten = etTenSV.getText().toString().trim();
                String lop = etLop.getText().toString().trim();
                String khuVuc = etKhuVuc.getText().toString().trim();
                String ghiChu = etGhiChu.getText().toString().trim();

                SimpleDateFormat appFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String timeStr = appFormat.format(selectedCal.getTime());

                if (ma.isEmpty() || ten.isEmpty() || khuVuc.isEmpty()) {
                    Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                JDBCService.insertCleaningSchedule(ma, ten, lop, khuVuc, ghiChu, timeStr, success -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(getContext(), "Thêm lịch thành công!", Toast.LENGTH_SHORT).show();
                                loadSchedulesFromDB();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getContext(), "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            });
        }

        dialog.show();
    }
}