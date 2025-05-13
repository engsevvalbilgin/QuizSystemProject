package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "questions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)  // Explicitly declare strategy
@DiscriminatorColumn(name = "question_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "number", nullable = false)
    private int number;

    @Column(name = "question_sentence", nullable = false, length = 1000)
    private String questionSentence;

    
    @ManyToOne(fetch = FetchType.LAZY) // Consider EAGER if type is always needed with Question
    @JoinColumn(name = "question_type_id", nullable = false) // Foreign key column in 'questions' table
    private QuestionType type;
    
    @OneToOne(cascade = CascadeType.ALL) 
    @JoinColumn(name = "question_answer_id", referencedColumnName = "id")
    private QuestionAnswer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    public static Question createQuestion() {
        return new Question();
    }
}
