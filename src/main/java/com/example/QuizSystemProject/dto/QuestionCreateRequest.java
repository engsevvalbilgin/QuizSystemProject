package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;

import java.util.List;

public class QuestionCreateRequest {

    @NotNull(message = "Soru numarası boş olamaz")
    @Min(value = 1, message = "Soru numarası en az 1 olmalı")
    private Integer number;

    @NotBlank(message = "Soru metni boş olamaz")
    @Size(max = 5000, message = "Soru metni çok uzun")
    private String questionSentence;

    @Size(max = 1000, message = "Doğru cevap metni çok uzun")
    private String correctAnswerText;

    @NotNull(message = "Soru tipi ID'si boş olamaz")
    private int questionTypeId;

    @NotNull(message = "Soru puanı boş olamaz")
    @Min(value = 1, message = "Soru puanı en az 1 olmalı")
    private int points = 1;

    @Valid
    private List<OptionCreateRequest> options;

    public QuestionCreateRequest() {
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getQuestionSentence() {
        return questionSentence;
    }

    public void setQuestionSentence(String questionSentence) {
        this.questionSentence = questionSentence;
    }

    public String getCorrectAnswerText() {
        return correctAnswerText;
    }

    public void setCorrectAnswerText(String correctAnswerText) {
        this.correctAnswerText = correctAnswerText;
    }

    public int getQuestionTypeId() {
        return questionTypeId;
    }

    public void setQuestionTypeId(int questionTypeId) {
        this.questionTypeId = questionTypeId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public List<OptionCreateRequest> getOptions() {
        return options;
    }

    public void setOptions(List<OptionCreateRequest> options) {
        this.options = options;
    }
}