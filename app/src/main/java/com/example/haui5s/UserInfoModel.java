package com.example.haui5s;

public class UserInfoModel {
    public String userCode;
    public String fullName;
    public String major;
    public String classInfo; // Sử dụng classInfo thay cho class
    public String course;
    public String phone; // <-- THÊM BIẾN NÀY
    public String email;

    // THÊM THAM SỐ phone VÀO CONSTRUCTOR
    public UserInfoModel(String userCode, String fullName, String major,
                         String classInfo, String course, String phone, String email) {
        this.userCode = userCode;
        this.fullName = fullName;
        this.major = major;
        this.classInfo = classInfo;
        this.course = course;
        this.phone = phone;
        this.email = email;
    }

    public String getUserCode() { return userCode; }
    public String getFullName() { return fullName; }
    public String getMajor() { return major; }
    public String getClassInfo() { return classInfo; }
    public String getCourse() { return course; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}