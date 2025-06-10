package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) 
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
    
    @Column(name = "points", nullable = false)
    private int points = 1; 

    @Column(name = "correct_answer_text", length = 1000) 
    private String correctAnswerText;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "question_type_id", nullable = false) 
    private QuestionType type;
    
    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private QuestionAnswer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<Option> options = new ArrayList<>();




    public static Question createQuestion() {
        return new Question();
    }

    public void addOption(Option option) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(option);
        if (option != null) { 
            option.setQuestion(this); 
        }
    }

    public void removeOption(Option option) {
        if (this.options != null) {
            this.options.remove(option);
        }
        if (option != null) { 
            option.setQuestion(null); 
        }
    }

    public String getCorrectAnswerText() {
        return correctAnswerText;
    }

    public void setCorrectAnswerText(String correctAnswerText) {
        this.correctAnswerText = correctAnswerText;
    }
}
