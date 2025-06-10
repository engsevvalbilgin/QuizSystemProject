package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.*;
import java.util.List;
import java.util.stream.Collectors;

public class AnswerAttemptResponse {

    private int id;
    private int questionId;
    private int questionNumber;
    private String questionSentence;

    private String submittedAnswerText;
    private List<String> selectedOptionTexts;

    private boolean isCorrect;

    public AnswerAttemptResponse() {
    }

    public AnswerAttemptResponse(AnswerAttempt attempt) {
        this.id = attempt.getId();

        if (attempt.getQuestion() != null) {
            this.questionId = attempt.getQuestion().getId();
            this.questionNumber = attempt.getQuestion().getNumber();
            this.questionSentence = attempt.getQuestion().getQuestionSentence();
        } else {
            this.questionId = -1;
            this.questionNumber = 0;
            this.questionSentence = "Bilinmeyen Soru";
        }

        this.submittedAnswerText = attempt.getSubmittedAnswerText();

        if (attempt.getSelectedOptions() != null && !attempt.getSelectedOptions().isEmpty()) {
            this.selectedOptionTexts = attempt.getSelectedOptions().stream()
                    .map(Option::getText)
                    .collect(Collectors.toList());
        } else {
            this.selectedOptionTexts = List.of();
        }

        this.isCorrect = attempt.isCorrect();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestionSentence() {
        return questionSentence;
    }

    public void setQuestionSentence(String questionSentence) {
        this.questionSentence = questionSentence;
    }

    public String getSubmittedAnswerText() {
        return submittedAnswerText;
    }

    public void setSubmittedAnswerText(String submittedAnswerText) {
        this.submittedAnswerText = submittedAnswerText;
    }

    public List<String> getSelectedOptionTexts() {
        return selectedOptionTexts;
    }

    public void setSelectedOptionTexts(List<String> selectedOptionTexts) {
        this.selectedOptionTexts = selectedOptionTexts;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}