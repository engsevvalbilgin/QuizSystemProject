package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.Question;
import java.util.List;
import java.util.stream.Collectors;

public class TeacherQuestionResponse {

    private int id;
    private int number;
    private String questionSentence;
    private String questionType;
    private int points;

    private List<TeacherOptionResponse> options;

    public TeacherQuestionResponse() {
    }

    public TeacherQuestionResponse(Question question) {
        this.id = question.getId();
        this.number = question.getNumber();
        this.questionSentence = question.getQuestionSentence();
        this.questionType = question.getType() != null ? question.getType().getTypeName() : null;
        this.points = question.getPoints();

        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            this.options = question.getOptions().stream()
                    .map(TeacherOptionResponse::new)
                    .collect(Collectors.toList());
        } else {
            this.options = List.of();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getQuestionSentence() {
        return questionSentence;
    }

    public void setQuestionSentence(String questionSentence) {
        this.questionSentence = questionSentence;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public List<TeacherOptionResponse> getOptions() {
        return options;
    }

    public void setOptions(List<TeacherOptionResponse> options) {
        this.options = options;
    }
}
