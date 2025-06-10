package com.example.QuizSystemProject.dto;

import java.util.Map;

public class QuizStatsResponse {

    private int quizId;
    private String quizName;
    private long totalAttempts;
    private Double averageScore;
    private Integer highestScore;
    private Integer lowestScore;
    private long totalQuestions;

    public QuizStatsResponse() {
    }

    public QuizStatsResponse(int quizId, String quizName, long totalAttempts, Double averageScore, Integer highestScore,
            Integer lowestScore, long totalQuestions) {
        this.quizId = quizId;
        this.quizName = quizName;
        this.totalAttempts = totalAttempts;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
        this.totalQuestions = totalQuestions;
    }

    public QuizStatsResponse(Map<String, Object> statsMap) {
        this.quizId = ((Number) statsMap.get("quizId")).intValue();
        this.quizName = (String) statsMap.get("quizName");
        this.totalAttempts = ((Number) statsMap.get("totalAttempts")).longValue();
        this.averageScore = (Double) statsMap.get("averageScore");
        this.highestScore = (Integer) statsMap.get("highestScore");
        this.lowestScore = (Integer) statsMap.get("lowestScore");
        this.totalQuestions = ((Number) statsMap.get("totalQuestions")).longValue(); 
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuizName() {
        return quizName;
    }

    public void setQuizName(String quizName) {
        this.quizName = quizName;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Integer getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(Integer highestScore) {
        this.highestScore = highestScore;
    }

    public Integer getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(Integer lowestScore) {
        this.lowestScore = lowestScore;
    }

    public long getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

}