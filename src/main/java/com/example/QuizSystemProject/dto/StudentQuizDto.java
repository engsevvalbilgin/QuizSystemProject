package com.example.QuizSystemProject.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentQuizDto {
    private int id;
    private String name;
    private String description;
    private int durationMinutes;
    private boolean active;
    private String topic;
    private LocalDateTime createdAt;
    private TeacherDto teacher;
    private int questionCount;
    private boolean attempted;
    
    @Data
    public static class TeacherDto {
        private int id;
        private String name;
        private String surname;
    }
}
