package com.example.QuizSystemProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedQuizAttemptDto {
    private int id;
    private int quizId;
    private String quizName;
    private String teacherName;
    private String topic;
    private String description;
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
    private List<QuestionResultDto> questionResults;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResultDto {
        private int questionId;
        private String questionText;
        private String questionType;
        private Integer number;
        private List<OptionDto> options;
        private List<Integer> selectedOptionIds;
        private String submittedTextAnswer;
        private boolean isCorrect;
        private double earnedPoints;
        private double maxPoints;
        private String correctAnswerText;
        private String aiExplanation; // AI'nin yaptığı değerlendirme açıklaması
        private Integer aiScore; // AI'nin verdiği puan
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {
        private int id;
        private String text;
        private boolean isCorrect;
    }
}
