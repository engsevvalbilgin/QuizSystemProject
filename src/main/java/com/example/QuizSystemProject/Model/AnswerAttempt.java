package com.example.QuizSystemProject.Model;

import jakarta.persistence.*; 
import java.util.HashSet; 
import java.util.Objects; 
import java.util.Set; 

@Entity 
@Table(name = "answer_attempts") 
public class AnswerAttempt {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id; 

    @ManyToOne 
    @JoinColumn(name = "quiz_session_id", nullable = false) 
    private QuizSession quizSession; 

    @ManyToOne 
    @JoinColumn(name = "question_id", nullable = false) 
    private Question question; 

    @Column(columnDefinition = "TEXT") 
    private String submittedAnswerText; 

    
    
    @ManyToMany 
    @JoinTable(
        name = "answer_attempt_selected_options", 
        joinColumns = @JoinColumn(name = "answer_attempt_id"), 
        inverseJoinColumns = @JoinColumn(name = "option_id") 
    )
    private Set<Option> selectedOptions = new HashSet<>(); 

    @Column(nullable = false) 
    private boolean isCorrect = false; 
    
    @Column(nullable = false) 
    private int earnedPoints = 0; 
    
    @Column(columnDefinition = "TEXT") 
    private String aiExplanation; 
    
    @Column
    private Integer aiScore; 

    public AnswerAttempt() {
         this.selectedOptions = new HashSet<>(); 
    }

    public AnswerAttempt(QuizSession quizSession, Question question, String submittedAnswerText) {
        this.quizSession = quizSession; 
        this.question = question; 
        this.submittedAnswerText = submittedAnswerText; 
        this.isCorrect = false; 
        this.selectedOptions = new HashSet<>(); 
    }
    
    public AnswerAttempt(QuizSession quizSession, Question question, Set<Option> selectedOptions) {
        this.quizSession = quizSession; 
        this.submittedAnswerText = null; 
        this.isCorrect = false; 
        this.selectedOptions = selectedOptions != null ? selectedOptions : new HashSet<>(); 
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; } 

    public QuizSession getQuizSession() { return quizSession; }
    public void setQuizSession(QuizSession quizSession) { this.quizSession = quizSession; } 

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; } 

    public String getSubmittedAnswerText() { return submittedAnswerText; }
    public void setSubmittedAnswerText(String submittedAnswerText) { this.submittedAnswerText = submittedAnswerText; }

    public Set<Option> getSelectedOptions() { return selectedOptions; }
    public void setSelectedOptions(Set<Option> selectedOptions) { this.selectedOptions = selectedOptions; } 

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
    
    public int getEarnedPoints() { return earnedPoints; }
    public void setEarnedPoints(int points) { this.earnedPoints = points; }
    
    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }
    
    public Integer getAiScore() { return aiScore; }
    public void setAiScore(Integer aiScore) { this.aiScore = aiScore; }
    
    public void setIsCorrect(boolean correct) { isCorrect = correct; }
    
    public String getTextAnswer() { return submittedAnswerText; }
    public void setTextAnswer(String text) { this.submittedAnswerText = text; }

    
    public void addSelectedOption(Option option) {
        this.selectedOptions.add(option);
    }

    public void removeSelectedOption(Option option) {
        this.selectedOptions.remove(option);
    }


    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerAttempt that = (AnswerAttempt) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AnswerAttempt{" +
               "id=" + id +
               ", quizSessionId=" + (quizSession != null ? quizSession.getId() : "null") +
               ", questionId=" + (question != null ? question.getId() : "null") +
               ", submittedAnswerText='" + (submittedAnswerText != null ? submittedAnswerText : "null") + '\'' +
               ", isCorrect=" + isCorrect +
               ", selectedOptionsCount=" + (selectedOptions != null ? selectedOptions.size() : 0) +
               '}';
    }

}
