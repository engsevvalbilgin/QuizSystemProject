package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity

 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestion extends Question {

    @OneToMany(mappedBy = "testQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Option> options;

    public TestQuestion(int quizId) {
        
    }

    public TestQuestion(String questionSentence, List<Option> options) {
         // quizId yerine uygun parametre kullanılabilir
        this.options = options;
    }

    public String askAIToAnswerQuestion(String questionSentence, List<Option> options) {
        return "AI thinks the best answer is: " +
                (options != null && !options.isEmpty() ? options.get(0).getText() : "N/A");
    }
}
