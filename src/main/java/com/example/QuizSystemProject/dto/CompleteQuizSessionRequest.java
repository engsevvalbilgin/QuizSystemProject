package com.example.QuizSystemProject.dto;

import lombok.Data;
import java.util.List;

@Data
public class CompleteQuizSessionRequest {
    private int quizSessionId;
    private List<QuestionAnswerDto> answers;
    
    @Data
    public static class QuestionAnswerDto {
        private int questionId;
        private List<Integer> selectedOptionIds;
        private String openEndedAnswer; 
    }
}
