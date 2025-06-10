package com.example.QuizSystemProject.dto; 
import com.example.QuizSystemProject.Model.QuizSession; 
import java.time.Duration; 
import java.time.LocalDateTime;
import java.util.List; 
import java.util.stream.Collectors; 

public class QuizSessionDetailsResponse {

     private int id;
     private int quizId;
     private String quizName;
     private LocalDateTime startTime;
     private LocalDateTime endTime; 
     private int score; 
     private int durationMinutes; 

     private int studentId;
     private String studentUsername;

     private List<AnswerAttemptResponse> answers; 
     public QuizSessionDetailsResponse() {
     }

     public QuizSessionDetailsResponse(QuizSession session) {
          this.id = session.getId();

          if (session.getQuiz() != null) {
               this.quizId = session.getQuiz().getId();
               this.quizName = session.getQuiz().getName();
          } else {
               this.quizId = -1;
               this.quizName = "Bilinmeyen Quiz";
          }

          this.startTime = session.getStartTime();
          this.endTime = session.getEndTime();
          this.score = session.getScore();

          if (session.getStartTime() != null && session.getEndTime() != null) {
               Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
               this.durationMinutes = (int) duration.toMinutes();
          } else {
               this.durationMinutes = 0;
          }

          if (session.getStudent() != null) {
               this.studentId = session.getStudent().getId();
               this.studentUsername = session.getStudent().getUsername();
          } else {
               this.studentId = -1;
               this.studentUsername = "Bilinmeyen Öğrenci";
          }

          if (session.getAnswers() != null && !session.getAnswers().isEmpty()) {
               this.answers = session.getAnswers().stream()
                         .map(AnswerAttemptResponse::new)
                         .collect(Collectors.toList());
          } else {
               this.answers = List.of();
          }
     }

     public int getId() {
          return id;
     }

     public void setId(int id) {
          this.id = id;
     }

     public int getQuizId() {
          return quizId;
     }

     public void setQuizId(int quizId) {
          this.quizId = quizId;
     }

     public String getQuizName() {
          return quizName;
     }

     public void setQuizName(String quizName) {
          this.quizName = quizName;
     }

     public LocalDateTime getStartTime() {
          return startTime;
     }

     public void setStartTime(LocalDateTime startTime) {
          this.startTime = startTime;
     }

     public LocalDateTime getEndTime() {
          return endTime;
     }

     public void setEndTime(LocalDateTime endTime) {
          this.endTime = endTime;
     }

     public int getScore() {
          return score;
     }

     public void setScore(int score) {
          this.score = score;
     }

     public int getDurationMinutes() {
          return durationMinutes;
     }

     public void setDurationMinutes(int durationMinutes) {
          this.durationMinutes = durationMinutes;
     }

     public int getStudentId() {
          return studentId;
     }

     public void setStudentId(int studentId) {
          this.studentId = studentId;
     }

     public String getStudentUsername() {
          return studentUsername;
     }

     public void setStudentUsername(String studentUsername) {
          this.studentUsername = studentUsername;
     }

     public List<AnswerAttemptResponse> getAnswers() {
          return answers;
     }

     public void setAnswers(List<AnswerAttemptResponse> answers) {
          this.answers = answers;
     }

}