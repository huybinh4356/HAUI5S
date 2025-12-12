package com.example.haui5s;

public class ReportModel {
    // Để public để truy cập trực tiếp (item.scoreS1) cho nhanh, khớp với code Fragment của bạn
    public int id;
    public String reporterCode;
    public String reporterName;
    public String area;
    public String note;
    public String imageUrl;
    public String timestamp;
    public int status;
    public String handlerCode;
    public String resolutionNote;

    // Các điểm số
    public int scoreS1;
    public int scoreS2;
    public int scoreS3;
    public int scoreS4;
    public int scoreS5;
    public int finalEvaluation;

    // Constructor đầy đủ (Khớp với JDBCService)
    public ReportModel(int id, String reporterCode, String reporterName, String area,
                       String note, String imageUrl, String timestamp, int status,
                       String handlerCode, String resolutionNote,
                       int scoreS1, int scoreS2, int scoreS3, int scoreS4, int scoreS5,
                       int finalEvaluation) {
        this.id = id;
        this.reporterCode = reporterCode;
        this.reporterName = reporterName;
        this.area = area;
        this.note = note;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.status = status;
        this.handlerCode = handlerCode;
        this.resolutionNote = resolutionNote;
        this.scoreS1 = scoreS1;
        this.scoreS2 = scoreS2;
        this.scoreS3 = scoreS3;
        this.scoreS4 = scoreS4;
        this.scoreS5 = scoreS5;
        this.finalEvaluation = finalEvaluation;
    }
}