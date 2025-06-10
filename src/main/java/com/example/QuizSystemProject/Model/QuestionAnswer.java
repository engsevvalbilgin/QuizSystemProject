package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "answer", nullable = false)
    private String answer;

    @Column(name = "is_correct")
    private boolean isCorrect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "take_quiz_id", nullable = true) 
    private TakeQuiz takeQuiz;

    @OneToOne 
    @JoinColumn(name = "question_id")
    private Question question;
    
    public void checkAndSetCorrectAnswer(String correctAnswer) {
        this.isCorrect = this.answer != null && this.answer.equals(correctAnswer);
    }
}
