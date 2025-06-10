package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.User;
import java.time.LocalDateTime;
import com.example.QuizSystemProject.Model.Teacher;

public class UserResponse {

    private int id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String role;
    private boolean isActive;
    private boolean enabled;
    private String diplomaNumber;
    private String graduateSchool;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.isActive = user.isActive();
        this.enabled = user.isEnabled();
        if (user instanceof Teacher) {
            this.diplomaNumber = ((Teacher) user).getDiplomaNumber();
            this.graduateSchool = ((Teacher) user).getGraduateSchool();
        }
        this.createdAt = user.getCreatedDate();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDiplomaNumber() {
        return diplomaNumber;
    }

    public void setDiplomaNumber(String diplomaNumber) {
        this.diplomaNumber = diplomaNumber;
    }

    public String getGraduateSchool() {
        return graduateSchool;
    }

    public void setGraduateSchool(String graduateSchool) {
        this.graduateSchool = graduateSchool;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}