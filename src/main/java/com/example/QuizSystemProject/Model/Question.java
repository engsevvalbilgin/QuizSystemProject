package com.example.QuizSystemProject.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)  // Explicitly declare strategy
@DiscriminatorColumn(name = "question_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    // JPA relationships often benefit from toString/equalsAndHashCode exclusions
    // Consider adding @ToString(exclude = {"quiz", "options"})
    // and @EqualsAndHashCode(exclude = {"quiz", "options"}) if @Data or similar is used.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "number", nullable = false)
    private int number;

    @Column(name = "question_sentence", nullable = false, length = 1000)
    private String questionSentence;
    
    @Column(name = "points", nullable = false)
    private int points = 1; // Default value of 1 point

    
    @ManyToOne(fetch = FetchType.LAZY) // Consider EAGER if type is always needed with Question
    @JoinColumn(name = "question_type_id", nullable = false) // Foreign key column in 'questions' table
    private QuestionType type;
    
    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private QuestionAnswer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Option> options = new ArrayList<>();


    

    


    public static Question createQuestion() {
        return new Question();
    }

    // Helper methods for managing the bidirectional relationship with Option
    public void addOption(Option option) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(option);
        if (option != null) { // Add null check for safety
            option.setQuestion(this); // Assuming Option has setQuestion
        }
    }

    public void removeOption(Option option) {
        if (this.options != null) {
            this.options.remove(option);
        }
        if (option != null) { // Add null check for safety
            option.setQuestion(null); // Assuming Option has setQuestion
        }
    }
}
