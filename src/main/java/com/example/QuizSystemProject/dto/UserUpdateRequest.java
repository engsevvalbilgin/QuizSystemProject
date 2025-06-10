package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

    @Size(max = 50, message = "Ad 50 karakterden uzun olamaz")
    private String name;

    @Size(max = 50, message = "Soyad 50 karakterden uzun olamaz")
    private String surname;

    @Min(value = 0, message = "Yaş negatif olamaz")
    private Integer age;

    @Email(message = "Geçerli bir email formatı girin")
    @Size(max = 100, message = "Email 100 karakterden uzun olamaz")
    private String email;

    @Size(min = 3, max = 50, message = "Kullanıcı adı 3 ile 50 karakter arasında olmalı")
    private String username;

    private Boolean isActive;

    public UserUpdateRequest() {
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}