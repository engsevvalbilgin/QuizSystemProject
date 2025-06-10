package com.example.QuizSystemProject.dto;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class StudentAnswerDto {
    private int questionId;
    private List<Integer> selectedOptionIds; 
    private String textAnswer; 
    private AnswerType answerType = AnswerType.MULTIPLE_CHOICE;
    private float score = 0; 
    private String aiExplanation; 
    
    public String getAnswerText() {
        return textAnswer;
    }
    
    public void setAnswerText(String answerText) {
        this.textAnswer = answerText;
    }
    
    public Integer getSelectedOptionId() {
        return selectedOptionIds != null && !selectedOptionIds.isEmpty() ? selectedOptionIds.get(0) : null;
    }
    
    public void setSelectedOptionId(Integer selectedOptionId) {
        if (selectedOptionId != null) {
            this.selectedOptionIds = new ArrayList<>();
            this.selectedOptionIds.add(selectedOptionId);
        } else {
            this.selectedOptionIds = null;
        }
    }
}
