package com.example.QuizSystemProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptDto {
    private int id;
    private int quizId;
    private String quizName;
    private String teacherName;
    private int studentId;
    private LocalDateTime startDate;
    private LocalDateTime completionDate;
    private int score;
    private int passingScore;
    private String status;
    private Integer timeSpentSeconds;
    private int correctAnswers;
    private int totalQuestions;
    private int totalPoints;
    private int earnedPoints;
}