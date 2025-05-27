package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.*;
import com.fasterxml.jackson.annotation.JsonProperty;

// Bu DTO, API yanıtlarında bir çoktan seçmeli soruya ait şık bilgilerini taşır.
// Öğretmen ve admin için kullanılacak sürüm - Doğru cevap bilgisi (isCorrect) DAHİL EDİLİR.
public class TeacherOptionResponse {

    private int id;
    private String text; // Şık metni
    
    @JsonProperty(value = "isCorrect") // JSON field name will always be isCorrect
    private boolean correct; // Renamed to 'correct' to avoid serialization issues

    // JPA için argümansız constructor
    public TeacherOptionResponse() {
    }

    // Option Entity'sinden bu DTO'ya dönüşüm yapmayı kolaylaştıran constructor
    public TeacherOptionResponse(Option option) {
        this.id = option.getId();
        this.text = option.getText();
        this.correct = option.isCorrect(); // isCorrect bilgisini entity'den alıyoruz
    }

    // Getter ve Setterlar
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    // Standard JavaBean getter and setter that follows JavaBean conventions
    @JsonProperty("isCorrect") // ensures the property appears as isCorrect in JSON
    public boolean isCorrect() { return correct; }
    
    public void setCorrect(boolean correct) { this.correct = correct; }
}
