package com.example.QuizSystemProject.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizResultDto {
    private int attemptId;
    private int quizId;
    private String quizName;
    private int score;
    private int totalQuestions;
    private int correctAnswers;
    private int timeSpent;
    private String completionDate;
    private boolean passed;
    private int passingScore;
    private int totalPoints;
    private int earnedPoints;
    private List<QuestionResultDto> questionResults;

    @Data
    public static class QuestionResultDto {
        private int questionId;
        private String questionText;
        private String questionTypeId;
        private Integer selectedAnswerId;
        private Integer correctAnswerId;
        private String textAnswer;
        private boolean requiresManualGrading;
        private boolean isCorrect;
        private boolean answered = true;
        private int points;
        private int earnedPoints;
        private String aiExplanation;
        private Integer aiScore;

        public int getEarnedPoints() {
            if ("OPEN_ENDED".equals(questionTypeId) && aiScore != null && aiScore > 0) {
                return aiScore;
            }
            return earnedPoints;
        }
    }
}
