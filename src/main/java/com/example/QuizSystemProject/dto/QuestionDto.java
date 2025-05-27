package com.example.QuizSystemProject.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDto {
    private int id;
    private String text; // Changed from questionText to text to match frontend
    private int questionTypeId; // 1: Çoktan seçmeli, 2: Açık uçlu
    private List<OptionDto> answers; // Changed from options to answers to match frontend
    private int points = 1; // Add this line with default value
}
