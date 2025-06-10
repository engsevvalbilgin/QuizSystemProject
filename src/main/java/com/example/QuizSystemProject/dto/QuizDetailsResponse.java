package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.Quiz;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class QuizDetailsResponse {

    private int id;
    private int teacherId;
    private String name;
    private String description;
    private String topic;
    private String teacherName;
    private Integer durationMinutes;
    private boolean isActive;
    private Date startDate;
    private Date endDate;
    private List<QuestionResponse> questions;

    public QuizDetailsResponse() {
    }

    public QuizDetailsResponse(Quiz quiz) {
        this.id = quiz.getId();

        if (quiz.getTeacher() != null) {
            this.teacherId = quiz.getTeacher().getId();
            this.teacherName = quiz.getTeacher().getName() + " " + quiz.getTeacher().getSurname();
        } else {
            this.teacherId = 0;
            this.teacherName = "Bilinmiyor";
        }

        this.name = quiz.getName();
        this.description = quiz.getDescription();
        this.topic = quiz.getTopic();
        this.durationMinutes = quiz.getDuration();
        this.isActive = quiz.isActive();
        this.startDate = quiz.getStartDate();
        this.endDate = quiz.getEndDate();

        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            this.questions = quiz.getQuestions().stream()
                    .map(QuestionResponse::new)
                    .collect(Collectors.toList());
        } else {
            this.questions = List.of();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<QuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }
}