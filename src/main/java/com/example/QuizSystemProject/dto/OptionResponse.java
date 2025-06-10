package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.*;

public class OptionResponse {

    private int id;
    private String text;

    public OptionResponse() {
    }

    public OptionResponse(Option option) {
        this.id = option.getId();
        this.text = option.getText();
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

}