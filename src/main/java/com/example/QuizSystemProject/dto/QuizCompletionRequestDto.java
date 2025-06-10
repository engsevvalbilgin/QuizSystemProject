package com.example.QuizSystemProject.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizCompletionRequestDto {
    private int quizId;
    private List<StudentAnswerDto> answers;
    private int timeSpent;
    
    @Data
    public static class StudentAnswerDto {
        private int questionId;
        private int answerId;     
        private String textAnswer;    }
}
