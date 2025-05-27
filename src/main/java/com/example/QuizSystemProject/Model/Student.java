package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Student entity that extends the base User class.
 * Represents a student user in the system.
 */
@Entity
@DiscriminatorValue("STUDENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users") // Explicitly set the table name to match the parent class
public class Student extends User {
    
    @Column(name = "school_name", nullable = false, length = 100, columnDefinition = "VARCHAR(100) DEFAULT 'Default School'")
    @Builder.Default
    private String schoolName = "Default School";

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TakeQuiz> takenQuizzes = new ArrayList<>();

    /**
     * Constructor for creating a new Student
     */
    public Student(String name, String surname, int age, String email, String username, 
                  String password, String schoolName) {
        super(name, surname, age, email, username, password, "ROLE_STUDENT");
        this.schoolName = schoolName != null ? schoolName : "Default School";
    }

    /**
     * Records a quiz attempt by the student
     * @param quiz The quiz being taken
     * @param score The score achieved in the quiz
     */
    /**
     * Records a quiz attempt by the student
     * @param quiz The quiz being taken
     */
    public void takeQuiz(Quiz quiz) {
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz cannot be null");
        }
        
        TakeQuiz takeQuiz = new TakeQuiz();
        takeQuiz.setStudent(this);
        takeQuiz.setQuiz(quiz);
        takeQuiz.setStartTime(new Date());
        
        if (takenQuizzes == null) {
            takenQuizzes = new ArrayList<>();
        }
        takenQuizzes.add(takeQuiz);
    }
    
    /**
     * Completes a quiz attempt
     * @param takeQuiz The quiz attempt to complete
     */
    public void completeQuiz(TakeQuiz takeQuiz) {
        if (takeQuiz != null) {
            takeQuiz.setEndTime(new Date());
        }
    }

    /**
     * Displays all quizzes taken by the student
     */
    public void showTakenQuizzes() {
        if (takenQuizzes != null && !takenQuizzes.isEmpty()) {
            takenQuizzes.forEach(System.out::println);
        } else {
            System.out.println("No quizzes taken yet.");
        }
    }

    @Override
    public void showUserDetails() {
        super.showUserDetails();
        System.out.println("School: " + getSchoolName());
        System.out.println("Quizzes Taken: " + (takenQuizzes != null ? takenQuizzes.size() : 0));
    }
    
    // Explicit getter and setter for schoolName to ensure proper mapping
    public String getSchoolName() {
        return schoolName;
    }
    
    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }
}
