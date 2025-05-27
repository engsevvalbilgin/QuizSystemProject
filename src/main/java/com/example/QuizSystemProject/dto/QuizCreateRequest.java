package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min; // Min anotasyonu için

public class QuizCreateRequest {

    @NotBlank(message = "Quiz adı boş olamaz")
    @Size(max = 100, message = "Quiz adı 100 karakterden uzun olamaz")
    private String name;

    @Size(max = 1000, message = "Açıklama 1000 karakterden uzun olamaz") // Opsiyonel alanlar için @NotBlank kullanmayız
    private String description;
    
    @Size(max = 100, message = "Konu 100 karakterden uzun olamaz")
    private String topic;

    // Süre dakika cinsinden, boş olabilir ama varsa pozitif olmalı
    @Min(value = 1, message = "Süre en az 1 dakika olmalı")
    private Integer durationMinutes;

    // Quiz'in aktif durumu, default olarak true
    private boolean isActive = true;

    // Getter ve Setterlar
    public QuizCreateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
}