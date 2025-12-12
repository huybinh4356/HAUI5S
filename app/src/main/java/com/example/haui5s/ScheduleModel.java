package com.example.haui5s;

public class ScheduleModel {
    public int id;
    public String personName;
    public String area;
    public String note;
    public String scheduleDate; // Dạng chuỗi: yyyy-MM-dd HH:mm:ss

    public ScheduleModel(int id, String personName, String area, String note, String scheduleDate) {
        this.id = id;
        this.personName = personName;
        this.area = area;
        this.note = note;
        this.scheduleDate = scheduleDate;
    }
}