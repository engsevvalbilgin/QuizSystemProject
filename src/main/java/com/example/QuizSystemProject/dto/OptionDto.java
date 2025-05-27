package com.example.QuizSystemProject.dto;

import lombok.Data;

@Data
public class OptionDto {
    private int id;
    private String text; // Changed from optionText to text to match frontend
}
