package com.quizsystem.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuizResult {
    private int id;
    private Date startTime;
    private Date endTime;
    private double score;
    private Quiz quiz;
    private Student student;
    private List<Answer> answers;
    
    public QuizResult() {
        this.answers = new ArrayList<>();
    }
    
    public QuizResult(int id, Date startTime, Date endTime, double score, Quiz quiz, Student student) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.score = score;
        this.quiz = quiz;
        this.student = student;
        this.answers = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
    
    public Quiz getQuiz() {
        return quiz;
    }
    
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public List<Answer> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }
    
    /**
     * Adds an answer to the quiz result
     * @param answer The answer to be added to the quiz result
     */
    public void addAnswer(Answer answer) {
        this.answers.add(answer);
    }
    
    /**
     * Removes an answer from the quiz result
     * @param answer The answer to be removed from the quiz result
     */
    public void removeAnswer(Answer answer) {
        this.answers.remove(answer);
    }
    
    @Override
    public String toString() {
        return "QuizResult{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", score=" + score +
                ", quiz=" + quiz +
                ", student=" + student +
                '}';
    }
}
