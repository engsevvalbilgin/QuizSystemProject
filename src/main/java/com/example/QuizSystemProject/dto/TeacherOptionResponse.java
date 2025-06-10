package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TeacherOptionResponse {

    private int id;
    private String text;

    @JsonProperty(value = "isCorrect")
    private boolean correct;

    public TeacherOptionResponse() {
    }

    public TeacherOptionResponse(Option option) {
        this.id = option.getId();
        this.text = option.getText();
        this.correct = option.isCorrect();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @JsonProperty("isCorrect")
    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
