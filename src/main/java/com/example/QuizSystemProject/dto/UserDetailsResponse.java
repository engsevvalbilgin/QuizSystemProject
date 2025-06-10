package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Model.Student;
import com.example.QuizSystemProject.Model.Teacher;
import java.time.LocalDateTime;

public class UserDetailsResponse {

    private int id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String role;
    private boolean isActive;
    private boolean enabled;

    private int age;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    private String schoolName;

    private String subject;
    private String graduateSchool;
    private String diplomaNumber;

    public UserDetailsResponse() {
    }

    public UserDetailsResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.isActive = user.isActive();
        this.enabled = user.isEnabled();
        this.age = user.getAge();
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();

        if (user instanceof Student) {
            this.schoolName = ((Student) user).getSchoolName();
        }

        if (user instanceof Teacher) {
            Teacher teacher = (Teacher) user;
            this.subject = teacher.getSubject();
            this.graduateSchool = teacher.getGraduateSchool();
            this.diplomaNumber = teacher.getDiplomaNumber();
        }
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGraduateSchool() {
        return graduateSchool;
    }

    public void setGraduateSchool(String graduateSchool) {
        this.graduateSchool = graduateSchool;
    }

    public String getDiplomaNumber() {
        return diplomaNumber;
    }

    public void setDiplomaNumber(String diplomaNumber) {
        this.diplomaNumber = diplomaNumber;
    }

}