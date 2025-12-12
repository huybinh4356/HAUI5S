package com.example.haui5s;

public class ScheduleModel {
    private int scheduleId;
    private String personName;
    private String area;
    private String note;
    private String scheduleDate;

    public ScheduleModel(int scheduleId, String personName, String area, String note, String scheduleDate) {
        this.scheduleId = scheduleId;
        this.personName = personName;
        this.area = area;
        this.note = note;
        this.scheduleDate = scheduleDate;
    }

    // CÁC HÀM GETTER BẮT BUỘC PHẢI CÓ
    public int getScheduleId() { return scheduleId; }
    public String getPersonName() { return personName; }
    public String getArea() { return area; }
    public String getNote() { return note; }
    public String getScheduleDate() { return scheduleDate; }
}