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
    private int timeSpent; // seconds
    private String completionDate;
    private boolean passed;
    private int passingScore;
    private int totalPoints; // Total possible points
    private int earnedPoints; // Total earned points
    private List<QuestionResultDto> questionResults;
    
    @Data
    public static class QuestionResultDto {
        private int questionId;
        private String questionText;
        private String questionTypeId;        // e.g., "MULTIPLE_CHOICE", "OPEN_ENDED"
        private Integer selectedAnswerId;      // For multiple choice questions
        private Integer correctAnswerId;       // For multiple choice questions
        private String textAnswer;         // For open-ended questions
        private boolean requiresManualGrading; // For open-ended questions that need manual/AI grading
        private boolean isCorrect;
        private boolean answered = true;   // Whether question was answered at all
        private int points;               // Maximum possible points for this question
        private int earnedPoints;        // Points earned for this question
        private String aiExplanation;      // AI's explanation for open-ended question scoring
        private Integer aiScore;
        
        // Override the getEarnedPoints method to ensure it returns aiScore for open-ended questions if available
        public int getEarnedPoints() {
            // If this is an open-ended question with an AI score, use that
            if ("OPEN_ENDED".equals(questionTypeId) && aiScore != null && aiScore > 0) {
                return aiScore;
            }
            return earnedPoints;
        }
    }
}
