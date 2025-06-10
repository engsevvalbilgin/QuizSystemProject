package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class AnswerSubmissionRequest {

    @NotNull(message = "Soru ID'si boş olamaz")
    private int questionId;

    @Size(max = 5000, message = "Cevap metni çok uzun")
    private String submittedAnswerText;

    private Set<Integer> selectedOptionIds;

    public AnswerSubmissionRequest() {
    }

    public AnswerSubmissionRequest(int questionId, String submittedAnswerText, Set<Integer> selectedOptionIds) {
        this.questionId = questionId;
        this.submittedAnswerText = submittedAnswerText;
        this.selectedOptionIds = selectedOptionIds;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getSubmittedAnswerText() {
        return submittedAnswerText;
    }

    public void setSubmittedAnswerText(String submittedAnswerText) {
        this.submittedAnswerText = submittedAnswerText;
    }

    public Set<Integer> getSelectedOptionIds() {
        return selectedOptionIds;
    }

    public void setSelectedOptionIds(Set<Integer> selectedOptionIds) {
        this.selectedOptionIds = selectedOptionIds;
    }
}