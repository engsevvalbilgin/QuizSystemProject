package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") 
    private int id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false) 
    private Question question;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;
    
    @ManyToOne
    @JoinColumn(name = "test_question_id")
    private TestQuestion testQuestion;

    public static Option createOption(Question question, String text, boolean isCorrect) {
        Option option = new Option();
        option.setQuestion(question);
        option.setText(text);
        option.setCorrect(isCorrect);
        return option;
    }

}
