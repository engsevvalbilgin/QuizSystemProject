package com.example.QuizSystemProject.Model;


import jakarta.persistence.*; 
import java.time.LocalDateTime; 
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; 

@Entity 
@Table(name = "quiz_sessions") 
public class QuizSession {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id; 

    @ManyToOne 
    @JoinColumn(name = "student_id", nullable = false) 
    private User student; 

    @ManyToOne 
    @JoinColumn(name = "quiz_id", nullable = false) 
    private Quiz quiz; 

    @Column(nullable = false) 
    private LocalDateTime startTime; 

    @Column 
    private LocalDateTime endTime;

    @Column(nullable = false) 
    private int score = 0; 
    
    @Column(nullable = false) 
    private int earnedPoints = 0; 
    
    @Column(nullable = false)
    private int correctAnswers = 0; 
    
    @Column(nullable = false)
    private boolean completed = false; 
    
    @Column
    private Integer timeSpentSeconds; 

    @OneToMany(mappedBy = "quizSession", cascade = CascadeType.ALL, orphanRemoval = true) 
    private List<AnswerAttempt> answers = new ArrayList<>(); 

    public QuizSession() {
         this.answers = new ArrayList<>(); 
    }

    public QuizSession(User student, Quiz quiz) {
        this.student = student;
        this.quiz = quiz;
        this.startTime = LocalDateTime.now(); 
        this.score = 0; 
        this.answers = new ArrayList<>(); 
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; } 

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public int getEarnedPoints() { return earnedPoints; }
    public void setEarnedPoints(int earnedPoints) { this.earnedPoints = earnedPoints; }
    
    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    public Integer getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(Integer timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }
    
   
    public int calculateTimeSpentSeconds() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return (int) java.time.Duration.between(startTime, endTime).getSeconds();
    }
    
   
    public boolean isSubmitted() {
        return endTime != null;
    }

    public List<AnswerAttempt> getAnswers() { return answers; }
    public void setAnswers(List<AnswerAttempt> answers) { this.answers = answers; }

    public void addAnswerAttempt(AnswerAttempt answerAttempt) {
        answers.add(answerAttempt);
        answerAttempt.setQuizSession(this); 
    }
    public void removeAnswerAttempt(AnswerAttempt answerAttempt) {
        answers.remove(answerAttempt);
        answerAttempt.setQuizSession(null); 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizSession that = (QuizSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "QuizSession{" +
               "id=" + id +
               ", student=" + (student != null ? student.getUsername() : "null") +
               ", quiz=" + (quiz != null ? quiz.getName() : "null") +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", score=" + score +
               '}';
    }
}

