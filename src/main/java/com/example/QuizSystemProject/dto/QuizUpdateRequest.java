package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

public class QuizUpdateRequest {

    // Ad güncellenecekse boş olamaz ve boyutu kontrol edilmeli
    @NotBlank(message = "Quiz adı boş olamaz")
    @Size(max = 100, message = "Quiz adı 100 karakterden uzun olamaz")
    private String name;

    @Size(max = 1000, message = "Açıklama 1000 karakterden uzun olamaz")
    private String description;

    @Min(value = 1, message = "Süre en az 1 dakika olmalı")
    private Integer durationMinutes;

    @NotNull(message = "Aktif durumu boş olamaz") // Boolean alanlar için @NotNull kullanılır
    private Boolean isActive; // primitive boolean yerine Boolean kullanmak null değer alabilmesini sağlar DTO'larda

    // Getter ve Setterlar
    public QuizUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Boolean isActive() { return isActive; } // boolean alanlar için isXxx() convention'ı
    public void setActive(Boolean active) { isActive = active; }
}