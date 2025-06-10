package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangeEmailRequest {

    @NotBlank(message = "Yeni email alanı boş olamaz")
    @Email(message = "Geçerli bir email formatı girin")
    @Size(max = 100, message = "Email 100 karakterden uzun olamaz")
    private String newEmail;

    @NotBlank(message = "Şifre alanı boş olamaz")
    private String password;

    public ChangeEmailRequest() {
    }

    public ChangeEmailRequest(String newEmail, String password) {
        this.newEmail = newEmail;
        this.password = password;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
