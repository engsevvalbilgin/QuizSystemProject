package com.example.QuizSystemProject.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDto {
    private int id;
    private String text;
    private int questionTypeId;
    private List<OptionDto> answers;
    private int points = 1;
}
