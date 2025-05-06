package com.quizsystem.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Quiz {
    private int id;
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;
    private int duration; // in minutes
    private boolean isActive;
    private Course course;
    private List<Question> questions;
    private List<QuizResult> quizResults;
    
    public Quiz() {
        this.questions = new ArrayList<>();
        this.quizResults = new ArrayList<>();
    }
    
    public Quiz(int id, String title, String description, Date startDate, Date endDate, 
                int duration, boolean isActive, Course course) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.isActive = isActive;
        this.course = course;
        this.questions = new ArrayList<>();
        this.quizResults = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public Course getCourse() {
        return course;
    }
    
    public void setCourse(Course course) {
        this.course = course;
    }
    
    public List<Question> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    
    /**
     * Adds a question to the quiz
     * @param question The question to be added to the quiz
     */
    public void addQuestion(Question question) {
        this.questions.add(question);
    }
    
    /**
     * Removes a question from the quiz
     * @param question The question to be removed from the quiz
     */
    public void removeQuestion(Question question) {
        this.questions.remove(question);
    }
    
    public List<QuizResult> getQuizResults() {
        return quizResults;
    }
    
    public void setQuizResults(List<QuizResult> quizResults) {
        this.quizResults = quizResults;
    }
    
    /**
     * Adds a quiz result to the quiz
     * @param quizResult The quiz result to be added to the quiz
     */
    public void addQuizResult(QuizResult quizResult) {
        this.quizResults.add(quizResult);
    }
    
    /**
     * Removes a quiz result from the quiz
     * @param quizResult The quiz result to be removed from the quiz
     */
    public void removeQuizResult(QuizResult quizResult) {
        this.quizResults.remove(quizResult);
    }
    
    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", duration=" + duration +
                ", isActive=" + isActive +
                ", course=" + course +
                '}';
    }
}
