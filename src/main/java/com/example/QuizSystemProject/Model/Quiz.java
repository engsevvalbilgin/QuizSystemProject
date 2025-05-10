package com.example.QuizSystemProject.Model;
import com.example.QuizSystemProject.Model.Question;
import com.example.QuizSystemProject.Model.QuestionAnswer;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int teacherId;
    private String name;

    @OneToMany(mappedBy = "quiz")
    private List<Question> questions;

    @OneToMany(mappedBy = "quiz")
    private List<QuestionAnswer> answers;

    private Date startDate;
    private Date endDate;
    private int duration;
    private boolean isActive;

    public Quiz() {
        this.questions = new ArrayList<>();
        this.isActive = true;
    }

    // Getter and Setter methods
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public List<QuestionAnswer> getAnswers() { return answers; }
    public void setAnswers(List<QuestionAnswer> answers) { this.answers = answers; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
