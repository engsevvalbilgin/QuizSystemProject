package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OptionCreateRequest {

    @NotBlank(message = "Şık metni boş olamaz")
    @Size(max = 500, message = "Şık metni 500 karakterden uzun olamaz")
    private String text;

    @NotNull(message = "Doğru cevap bilgisi boş olamaz")
    private Boolean isCorrect; // Boolean için @NotNull

    // Getter ve Setterlar
    public OptionCreateRequest() {}

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    // Jackson için getter metodu getXxx() formatında olmalı
    public Boolean getIsCorrect() { return isCorrect; }
    public void setCorrect(Boolean correct) { isCorrect = correct; }
    
    // Boolean özellikler için ek isXxx() yardımcı metodu
    public Boolean isCorrect() { return isCorrect; }
}