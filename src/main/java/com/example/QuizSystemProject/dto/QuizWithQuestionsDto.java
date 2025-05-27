package com.example.QuizSystemProject.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizWithQuestionsDto {
    private int id;
    private String name;
    private String description;
    private String topic;
    private int durationMinutes;
    private boolean active = true; // Default to true since we only load active quizzes
    private List<QuestionDto> questions;
}
