package com.example.QuizSystemProject.dto; 

import com.example.QuizSystemProject.Model.*;

import lombok.Data;
import java.time.Duration; 
import java.time.LocalDateTime;
@Data
public class QuizSessionResponse {

    private int id;
    private int quizId; 
    private String quizName;
    private LocalDateTime startTime;
    private LocalDateTime endTime; 
    private int score; 
    private int durationMinutes; 

    public QuizSessionResponse() {
    }

    public QuizSessionResponse(QuizSession session) {
        this.id = (int) session.getId();

        if (session.getQuiz() != null) {
            this.quizId = (int) session.getQuiz().getId();
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
        } else if (session.getStartTime() != null && session.getQuiz() != null && session.getQuiz().getDuration() != -1) {
             this.durationMinutes = 0; 
        } else {
             this.durationMinutes = 0;
        }
    }



}