package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import jakarta.validation.constraints.Email; // Email formatı için
import jakarta.validation.constraints.NotBlank; // Boş olamaz kontrolü için

// Parola sıfırlama isteği veya sadece email bilgisini göndermek için kullanılacak DTO.
public class EmailRequestDto {

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email formatı girin")
    private String email; // Email adresi

    // --- Constructorlar ---
    // Argümansız constructor
    public EmailRequestDto() {
    }

    // Email alan constructor
    public EmailRequestDto(String email) {
        this.email = email;
    }

    // --- Getter ve Setter ---
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}