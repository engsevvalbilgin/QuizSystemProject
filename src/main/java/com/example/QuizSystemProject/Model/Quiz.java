package com.example.QuizSystemProject.Model;
import com.example.QuizSystemProject.Model.Question;
import com.example.QuizSystemProject.Model.QuestionAnswer;
import com.example.QuizSystemProject.Model.Teacher;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
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
    private List<Question> questions = new ArrayList<>(); // Initialize here

    // @OneToMany
    // @JoinColumn(name = "quiz_id") 
    // private List<QuestionAnswer> answers; // This mapping is likely incorrect and needs review

    private Date startDate;
    private Date endDate;
    private int duration;
    private boolean isActive;
    private String description;
    public Quiz() {
        // questions list is now initialized at declaration
        this.isActive = true;
        name="a"; // Consider if default name 'a' is intended or placeholder
    }

    // Helper methods for managing the bidirectional relationship with Question
    public void addQuestion(Question question) {
        if (this.questions == null) {
            this.questions = new ArrayList<>();
        }
        this.questions.add(question);
        if (question != null) {
            question.setQuiz(this); // Assuming Question has setQuiz
        }
    }

    public void removeQuestion(Question question) {
        if (this.questions != null) {
            this.questions.remove(question);
        }
        if (question != null) {
            question.setQuiz(null); // Assuming Question has setQuiz
        }
    }
    // Manual getters and setters removed; Lombok's @Getter and @Setter will be used.
    // Note: The 'answers' list and its getter/setter were commented out due to problematic mapping.
}
