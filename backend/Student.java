package com.quizsystem.model;

import java.util.ArrayList;
import java.util.List;

public class Student extends User {
    private String studentNo;
    private List<QuizResult> quizResults;
    
    public Student() {
        super();
        this.quizResults = new ArrayList<>();
    }
    
    public Student(int id, String name, String surname, int age, String email, String password, String username, String studentNo) {
        super(id, name, surname, age, email, password, username);
        this.studentNo = studentNo;
        this.quizResults = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getStudentNo() {
        return studentNo;
    }
    
    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }
    
    public List<QuizResult> getQuizResults() {
        return quizResults;
    }
    
    public void setQuizResults(List<QuizResult> quizResults) {
        this.quizResults = quizResults;
    }
    
    /**
     * Adds a quiz result to the student's list of quiz results
     * @param quizResult The quiz result to be added
     */
    public void addQuizResult(QuizResult quizResult) {
        this.quizResults.add(quizResult);
    }
    
    /**
     * Removes a quiz result from the student's list of quiz results
     * @param quizResult The quiz result to be removed
     */
    public void removeQuizResult(QuizResult quizResult) {
        this.quizResults.remove(quizResult);
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", surname='" + getSurname() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", studentNo='" + studentNo + '\'' +
                '}';
    }
}
