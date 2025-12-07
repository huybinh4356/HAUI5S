package com.example.haui5s;

public class BorrowModel {
    private int borrowId;
    private String studentName;
    private String toolName;
    private String borrowDate;
    private int status;

    public BorrowModel(int borrowId, String studentName, String toolName, String borrowDate, int status) {
        this.borrowId = borrowId;
        this.studentName = studentName;
        this.toolName = toolName;
        this.borrowDate = borrowDate;
        this.status = status;
    }

    public int getBorrowId() { return borrowId; }
    public String getStudentName() { return studentName; }
    public String getToolName() { return toolName; }
    public String getBorrowDate() { return borrowDate; }
    public int getStatus() { return status; }
}