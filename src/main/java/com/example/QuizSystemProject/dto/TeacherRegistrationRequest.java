package com.example.QuizSystemProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TeacherRegistrationRequest extends UserCreationRequest {

    @NotBlank(message = "Subject cannot be blank")
    @Size(max = 100, message = "Subject must be at most 100 characters")
    private String subject;

    @NotBlank(message = "Graduate school cannot be blank")
    @Size(max = 100, message = "Graduate school must be at most 100 characters")
    private String graduateSchool;

    @NotBlank(message = "Diploma number cannot be blank")
    @Size(max = 50, message = "Diploma number must be at most 50 characters")
    private String diplomaNumber;

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
