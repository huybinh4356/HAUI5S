package com.example.haui5s;

public class UserInfoModel {
    public String userCode;
    public String fullName;
    public String major;
    public String classInfo;
    public String course;
    public String phone;
    public String email;


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