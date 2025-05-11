package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import jakarta.validation.constraints.Email; // Email formatı için
import jakarta.validation.constraints.NotBlank; // Boş olamaz kontrolü için
import jakarta.validation.constraints.Size; // Uzunluk kontrolü için

// Bu DTO, kullanıcı e-posta adresini değiştirmek için kullanılır.
public class EmailChangeRequest {

    @NotBlank(message = "Yeni email boş olamaz") // Yeni email adresi boş olamaz
    @Email(message = "Geçerli bir email formatı girin") // Geçerli email formatı kontrolü
    @Size(max = 100, message = "Email 100 karakterden uzun olamaz") // Maksimum uzunluk
    private String newEmail;

    // Getter ve Setterlar
    public EmailChangeRequest() {}

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }
}