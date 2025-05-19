package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import jakarta.validation.constraints.Email; // Email formatı için
import jakarta.validation.constraints.NotBlank; // Boş olamaz kontrolü için

// Parola sıfırlama isteği veya sadece email bilgisini göndermek için kullanılacak DTO.
public class EmailRequestDto {

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email formatı girin")
    private String email; // Email adresi

    private String verificationLink;
    private String subject;
    private String body;

    // --- Constructorlar ---
    // Argümansız constructor
    public EmailRequestDto() {
    }

    // Email alan constructor
    public EmailRequestDto(String email) {
        this.email = email;
    }

    // Tüm alanları alan constructor
    public EmailRequestDto(String email, String verificationLink, String subject, String body) {
        this.email = email;
        this.verificationLink = verificationLink;
        this.subject = subject;
        this.body = body;
    }

    // --- Getter ve Setter ---
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerificationLink() {
        return verificationLink;
    }

    public void setVerificationLink(String verificationLink) {
        this.verificationLink = verificationLink;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}