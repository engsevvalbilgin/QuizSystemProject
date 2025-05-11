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

    public Boolean isCorrect() { return isCorrect; } // boolean için isXxx()
    public void setCorrect(Boolean correct) { isCorrect = correct; }
}