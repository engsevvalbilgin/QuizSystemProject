package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.NotBlank;

public class RoleChangeRequest {

    @NotBlank(message = "Yeni rol boş olamaz")
    private String newRole;

    public RoleChangeRequest() {
    }

    public RoleChangeRequest(String newRole) {
        this.newRole = newRole;
    }

    public String getNewRole() {
        return newRole;
    }

    public void setNewRole(String newRole) {
        this.newRole = newRole;
    }

}