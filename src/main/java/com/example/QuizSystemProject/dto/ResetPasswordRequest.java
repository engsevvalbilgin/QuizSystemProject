package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import jakarta.validation.constraints.NotBlank; // Boş olamaz kontrolü için
import jakarta.validation.constraints.Size; // Uzunluk kontrolü için

// Parola sıfırlama tamamlama isteği için kullanılacak DTO.
public class ResetPasswordRequest {

    @NotBlank(message = "Token boş olamaz")
    private String token; // Parola sıfırlama token'ı

    @NotBlank(message = "Yeni parola boş olamaz")
    @Size(min = 6, message = "Yeni parola en az 6 karakter olmalı") // Parola için minimum uzunluk kısıtlaması
    private String newPassword; // Kullanıcının yeni parolası (şifrelenmeden gelir)

    // --- Constructorlar ---
    // Argümansız constructor
    public ResetPasswordRequest() {
    }

    // Alanları alan constructor
    public ResetPasswordRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    // --- Getter ve Setterlar ---
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}