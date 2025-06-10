package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id") 
    private Teacher teacher;

    private String name;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<Question> questions = new ArrayList<>(); 


    

 

    private Date startDate;
    private Date endDate;
    private int duration;
    @Column(name = "is_active", nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
    private boolean isActive = true;
    
    public boolean isActive() {
        return isActive;
    }
    
    public int getPassingScore() {
        return passingScore;
    }
    
    public void setPassingScore(int passingScore) {
        this.passingScore = passingScore;
    }
    
   
    public int getTotalPoints() {
        int total = 0;
        if (questions != null) {
            for (Question question : questions) {
                total += question.getPoints() > 0 ? question.getPoints() : 1;
            }
        }
        return total > 0 ? total : questions != null ? questions.size() : 0;
    }
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 255)
    private String topic;
    private int passingScore = 60; 
    
    public Quiz() {
        this.isActive = true;
        this.name = ""; 
    }

    public void addQuestion(Question question) {
        if (this.questions == null) {
            this.questions = new ArrayList<>();
        }
        this.questions.add(question);
        if (question != null) {
            question.setQuiz(this); 
        }
    }

    public void removeQuestion(Question question) {
        if (this.questions != null) {
            this.questions.remove(question);
        }
        if (question != null) {
            question.setQuiz(null); 
        }
    }
}
