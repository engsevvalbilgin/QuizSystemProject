package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Teacher extends User {

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Quiz> quizzes;

    @Column(name = "subject")
    private String subject;

    @Column(name = "graduate_school")
    private String graduateSchool;

    @Column(name = "diploma_number")
    private String diplomaNumber;

    public void createQuiz(Quiz quiz) {
        quizzes.add(quiz);
        quiz.setTeacher(this); // Automatically associate the quiz with this teacher
    }

    public void showQuizzes() {
        for (Quiz quiz : quizzes) {
            System.out.println(quiz);
        }
    }

    @Override
    public void showUserDetails() {
         // Calls the base method from User class
        System.out.println("Quizzes: " + getQuizzes());
        System.out.println("Subject: " + getSubject());
        System.out.println("Graduate School: " + getGraduateSchool());
        System.out.println("Diploma: " + getDiplomaNumber());
    }

    @Override
    public void signIn(User user) {
        super.signIn(user); // Uses the signIn from User class
    }

    @Override
    public void logIn(User user) {
        super.logIn(user); // Uses the logIn from User class
    }

    @Override
    public void logOut(User user) {
        // Logic to log out
    }
}
