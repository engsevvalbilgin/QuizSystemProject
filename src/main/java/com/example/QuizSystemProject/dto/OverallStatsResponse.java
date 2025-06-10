package com.example.QuizSystemProject.dto;

import java.util.Map;

public class OverallStatsResponse {

    private int totalUsers;
    private int totalQuizzes;
    private int totalQuizSessions;
    private Double averageScore;

    public OverallStatsResponse() {
    }

    public OverallStatsResponse(int totalUsers, int totalQuizzes, int totalQuizSessions, Double averageScore) {
        this.totalUsers = totalUsers;
        this.totalQuizzes = totalQuizzes;
        this.totalQuizSessions = totalQuizSessions;
        this.averageScore = averageScore;
    }

    public OverallStatsResponse(Map<String, Object> statsMap) {
        this.totalUsers = ((Number) statsMap.get("totalUsers")).intValue();
        this.totalQuizzes = ((Number) statsMap.get("totalQuizzes")).intValue();
        this.totalQuizSessions = ((Number) statsMap.get("totalQuizSessions")).intValue();
        this.averageScore = (Double) statsMap.get("averageScore");
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(int totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public int getTotalQuizSessions() {
        return totalQuizSessions;
    }

    public void setTotalQuizSessions(int totalQuizSessions) {
        this.totalQuizSessions = totalQuizSessions;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

}