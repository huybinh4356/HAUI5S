package com.example.haui5s;

public class BorrowModel {
    private int borrowId;
    private String studentCode; // Thêm trường này cho khớp DB
    private String studentName;
    private String toolName;
    private String borrowDate;
    private int status;

    // Constructor đầy đủ
    public BorrowModel(int borrowId, String studentCode, String studentName, String toolName, String borrowDate, int status) {
        this.borrowId = borrowId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.toolName = toolName;
        this.borrowDate = borrowDate;
        this.status = status;
    }

    // Getters
    public int getBorrowId() { return borrowId; }
    public String getStudentCode() { return studentCode; }
    public String getStudentName() { return studentName; }
    public String getToolName() { return toolName; }
    public String getBorrowDate() { return borrowDate; }
    public int getStatus() { return status; }

    // Setter (Quan trọng để cập nhật trạng thái trên giao diện)
    public void setStatus(int status) {
        this.status = status;
    }
}