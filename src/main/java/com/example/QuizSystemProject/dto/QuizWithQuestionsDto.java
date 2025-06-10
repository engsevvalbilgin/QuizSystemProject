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
    private boolean active = true;
    private List<QuestionDto> questions;
}
