package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.QuizSession;
import com.example.QuizSystemProject.Model.User;

import java.util.List;


public class StudentOverallResultsResponse {

    private int id;
    private String name;
    private String surname;
    private int totalQuizzes;
    private double averageScore;
    private int successfulQuizzes;

    public StudentOverallResultsResponse() {
    }

    public StudentOverallResultsResponse(User student, List<QuizSession> sessions) {
        if (student != null) {
            this.id = student.getId();
            this.name = student.getName();
            this.surname = student.getSurname();
        } else {
            this.id = -1;
            this.name = "Bilinmeyen";
            this.surname = "Öğrenci";
        }

        this.totalQuizzes = sessions != null ? (int) sessions.stream()
                .map(session -> session.getQuiz().getId())
                .distinct()
                .count() : 0;

        double calculatedAverageScore = 0.0;
        if (sessions != null && !sessions.isEmpty()) {
            int totalScore = sessions.stream().mapToInt(QuizSession::getScore).sum();
            calculatedAverageScore = (double) totalScore / sessions.size();
        }
        this.averageScore = calculatedAverageScore;

        int successfulQuizzesCount = 0;
        if (sessions != null) {
            successfulQuizzesCount = (int) sessions.stream()
                    .filter(QuizSession::isCompleted)
                    .count();
        }
        this.successfulQuizzes = successfulQuizzesCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public int getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(int totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getSuccessfulQuizzes() {
        return successfulQuizzes;
    }

    public void setSuccessfulQuizzes(int successfulQuizzes) {
        this.successfulQuizzes = successfulQuizzes;
    }
}