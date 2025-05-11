package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import jakarta.validation.constraints.NotBlank; // Boş olamaz kontrolü için
import jakarta.validation.constraints.Size; // Uzunluk kontrolü için

// Bu DTO, kullanıcı parolasını değiştirmek için kullanılır.
public class PasswordChangeRequest {

    // Kullanıcı kendi parolasını değiştiriyorsa mevcut parolayı doğrulamak gerekir.
    // Admin değiştiriyorsa bu alan boş gelebilir veya dikkate alınmaz.
    // Bu yüzden @NotBlank koymuyoruz, boş veya null olabilir.
    private String currentPassword;

    @NotBlank(message = "Yeni parola boş olamaz") // Yeni parola kesinlikle boş olamaz
    @Size(min = 6, message = "Yeni parola en az 6 karakter olmalı") // Minimum uzunluk kısıtlaması
    private String newPassword;

    // Getter ve Setterlar
    public PasswordChangeRequest() {}

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}