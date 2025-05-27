package com.example.QuizSystemProject.dto;

import lombok.Data;

@Data
public class TeacherRequestDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String subject;
    private String graduateSchool;
    private String diplomaNumber;
    private String status;
}
