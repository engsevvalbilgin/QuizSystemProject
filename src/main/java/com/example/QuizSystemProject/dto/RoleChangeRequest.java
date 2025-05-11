package com.example.QuizSystemProject.dto; // Paket adınızın doğru olduğundan emin olun

import jakarta.validation.constraints.NotBlank; // Boş olamaz kontrolü için

// Bu DTO, kullanıcının rolünü değiştirmek için kullanılır.
public class RoleChangeRequest {

    @NotBlank(message = "Yeni rol boş olamaz")
    private String newRole; // Kullanıcının yeni rolü (örn: ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT)

    // --- Constructorlar ---
    // Argümansız constructor (Spring'in JSON'ı objeye dönüştürmesi için gerekli)
    public RoleChangeRequest() {
    }

    // Yeni rolü alan constructor (bu constructor eksikti, şimdi ekliyoruz)
    public RoleChangeRequest(String newRole) {
        this.newRole = newRole;
    }

    // --- Getter ve Setter ---
    // IDE ile otomatik oluşturabilirsiniz.

    public String getNewRole() {
        return newRole;
    }

    public void setNewRole(String newRole) {
        this.newRole = newRole;
    }

    // İsteğe bağlı olarak toString, equals, hashCode metotları eklenebilir.
}