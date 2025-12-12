package com.example.haui5s;

public class ReportModel {
    public int id;
    public String reporterCode;
    public String reporterName;
    public String area;
    public String note;
    public String imageUrl;
    public String reportTime;
    public int status;
    public String handlerCode;
    public String resolutionNote;
    public int s1, s2, s3, s4, s5;
    public int finalEvaluation;

    public ReportModel(int id, String reporterCode, String reporterName, String area,
                       String note, String imageUrl, String reportTime, int status,
                       String handlerCode, String resolutionNote,
                       int s1, int s2, int s3, int s4, int s5, int finalEvaluation) {
        this.id = id;
        this.reporterCode = reporterCode;
        this.reporterName = reporterName;
        this.area = area;
        this.note = note;
        this.imageUrl = imageUrl;
        this.reportTime = reportTime;
        this.status = status;
        this.handlerCode = handlerCode;
        this.resolutionNote = resolutionNote;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.s4 = s4;
        this.s5 = s5;
        this.finalEvaluation = finalEvaluation;
    }
}