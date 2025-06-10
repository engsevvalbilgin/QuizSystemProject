package com.example.QuizSystemProject.dto; 

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size; 

public class EmailChangeRequest {

    @NotBlank(message = "Yeni email boş olamaz") 
    @Email(message = "Geçerli bir email formatı girin") 
    @Size(max = 100, message = "Email 100 karakterden uzun olamaz") 
    private String newEmail;

    @NotBlank(message = "Parola boş olamaz")
    private String password;

    public EmailChangeRequest() {}

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