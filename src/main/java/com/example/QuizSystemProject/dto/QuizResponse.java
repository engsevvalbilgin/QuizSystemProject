package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.Quiz;
import com.example.QuizSystemProject.Model.Teacher;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizResponse {

    private int id;
    private int teacherId;
    private String name;
    private String description;
    private String topic;
    private String teacherName;
    private int durationMinutes;
    private boolean isActive;
    private int questionCount;

    public QuizResponse() {
    }

    public QuizResponse(Quiz quiz) {
        this.id = quiz.getId();
        if (quiz.getTeacher() != null) {
            this.teacherId = quiz.getTeacher().getId();
        } else {
            this.teacherId = 0;
        }
        this.name = quiz.getName();
        this.description = quiz.getDescription();
        this.topic = quiz.getTopic();

        if (quiz.getTeacher() != null) {
            Teacher teacher = quiz.getTeacher();
            this.teacherName = teacher.getName() != null && teacher.getSurname() != null
                    ? teacher.getName() + " " + teacher.getSurname()
                    : teacher.getUsername();
        } else {
            this.teacherName = "Bilinmiyor";
        }

        this.durationMinutes = quiz.getDuration();
        this.isActive = quiz.isActive();
        this.questionCount = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
}