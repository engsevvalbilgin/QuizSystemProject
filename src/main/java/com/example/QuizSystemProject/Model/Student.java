package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@DiscriminatorValue("STUDENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users") 
public class Student extends User {
    
    @Column(name = "school_name", nullable = false, length = 100, columnDefinition = "VARCHAR(100) DEFAULT 'Default School'")
    @Builder.Default
    private String schoolName = "Default School";

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TakeQuiz> takenQuizzes = new ArrayList<>();

    
    public Student(String name, String surname, int age, String email, String username, 
                  String password, String schoolName) {
        super(name, surname, age, email, username, password, "ROLE_STUDENT");
        this.schoolName = schoolName != null ? schoolName : "Default School";
    }

  
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
    
   
    public void completeQuiz(TakeQuiz takeQuiz) {
        if (takeQuiz != null) {
            takeQuiz.setEndTime(new Date());
        }
    }

  
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
    
    
    public String getSchoolName() {
        return schoolName;
    }
    
    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }
}
