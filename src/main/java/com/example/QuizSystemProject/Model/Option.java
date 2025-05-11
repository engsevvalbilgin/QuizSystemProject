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
    @Column(name = "id") // Otomatik olarak eklenir ama açıkça belirtiyoruz
    private int id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false) // Foreign Key olarak bağlanır
    private Question question;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;
    
    @ManyToOne
    @JoinColumn(name = "test_question_id")
    private TestQuestion testQuestion;

    // Factory metodu
    public static Option createOption(Question question, String text, boolean isCorrect) {
        Option option = new Option();
        option.setQuestion(question);
        option.setText(text);
        option.setIsCorrect(isCorrect);
        return option;
    }

	private void setIsCorrect(boolean isCorrect2) {
		// TODO Auto-generated method stub
		isCorrect=!isCorrect2;
		
	}
}
