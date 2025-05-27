package com.example.QuizSystemProject.dto;

import lombok.Data;

@Data
public class QuizSessionStartResponse {
    private int sessionId;
    private QuizWithQuestionsDto quiz;
}
