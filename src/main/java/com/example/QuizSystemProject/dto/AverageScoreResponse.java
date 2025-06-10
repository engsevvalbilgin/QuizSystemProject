package com.example.QuizSystemProject.dto;

public class AverageScoreResponse {

    private int studentId;
    private Double averageScore;

    public AverageScoreResponse() {
    }

    public AverageScoreResponse(int studentId, Double averageScore) {
        this.studentId = studentId;
        this.averageScore = averageScore;
    }

    public AverageScoreResponse(Double averageScore) {
        this.averageScore = averageScore;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

}