package com.example.QuizSystemProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenEndedEvaluationResultDto {
    private int earnedPoints;
    private String explanation;
}
