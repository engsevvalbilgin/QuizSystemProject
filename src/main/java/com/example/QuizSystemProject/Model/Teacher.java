package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.DiscriminatorValue;
@Entity

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("Teacher")   
public class Teacher extends User {

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Quiz> quizzes = new ArrayList<>();

    @Column(name = "subject")
    private String subject;

    @Column(name = "graduate_school")
    private String graduateSchool;

    @Column(name = "diploma_number")
    private String diplomaNumber;

    public void createQuiz(Quiz quiz) {
        quizzes.add(quiz);
        quiz.setTeacher(this); 
    }

    public void showQuizzes() {
        for (Quiz quiz : quizzes) {
            System.out.println(quiz);
        }
    }

    @Override
    public void showUserDetails() {
        System.out.println("Quizzes: " + getQuizzes());
        System.out.println("Subject: " + getSubject());
        System.out.println("Graduate School: " + getGraduateSchool());
        System.out.println("Diploma: " + getDiplomaNumber());
    }

    @Override
    public void signIn(User user) {
        super.signIn(user); 
    }

    @Override
    public void logIn(User user) {
        super.logIn(user); 
    }

    @Override
    public void logOut(User user) {
        
    }
}
