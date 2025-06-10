package com.example.QuizSystemProject.dto; 

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailRequestDto {

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email formatı girin")
    private String email; 

    private String verificationLink;
    private String subject;
    private String body;

    public EmailRequestDto() {
    }

    public EmailRequestDto(String email) {
        this.email = email;
    }

    public EmailRequestDto(String email, String verificationLink, String subject, String body) {
        this.email = email;
        this.verificationLink = verificationLink;
        this.subject = subject;
        this.body = body;
    }

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