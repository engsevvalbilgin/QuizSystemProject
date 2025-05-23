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
    
    @Size(max = 100, message = "Konu 100 karakterden uzun olamaz")
    private String topic;

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
    
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    // Jackson ile doğru deserializasyon için hem isActive() hem de getIsActive() metodlarını ekliyoruz
    public Boolean isActive() { return isActive; } // boolean alanlar için isXxx() convention'ı
    public Boolean getIsActive() { return isActive; } // Alternatif getter (JSON deserialization için)
    
    // Setter metodu - hem setActive hem de setIsActive
    public void setActive(Boolean active) { isActive = active; }
    public void setIsActive(Boolean active) { isActive = active; } // JSON deserializasyon için
}