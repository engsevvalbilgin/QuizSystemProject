package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OptionUpdateRequest {

   

    @NotBlank(message = "Şık metni boş olamaz")
    @Size(max = 500, message = "Şık metni 500 karakterden uzun olamaz")
    private String text;

    @NotNull(message = "Doğru cevap bilgisi boş olamaz")
    private Boolean isCorrect;

    // Getter ve Setterlar
    public OptionUpdateRequest() {}

    


    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Boolean isCorrect() { return isCorrect; }
    public void setCorrect(Boolean correct) { isCorrect = correct; }
}