package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OptionUpdateRequest {

    @NotNull(message = "Option ID cannot be null")
    private Integer id;

    @NotBlank(message = "Şık metni boş olamaz")
    @Size(max = 500, message = "Şık metni 500 karakterden uzun olamaz")
    private String text;

    @NotNull(message = "Doğru cevap bilgisi boş olamaz")
    @JsonProperty(value = "isCorrect")
    private Boolean correct;

    public OptionUpdateRequest() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @JsonProperty("isCorrect")
    public Boolean isCorrect() {
        return correct;
    }

    @JsonProperty("isCorrect")
    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

}