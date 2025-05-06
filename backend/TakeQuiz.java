package com.quizprojesi2;
import java.util.Date;

public class TakeQuiz {
    private int id;
    private int studentId;
    private int quizId;
    private Date startTime;
    private Date endTime;

    public TakeQuiz() {}

    public TakeQuiz(int id, int studentId, int quizId) {
        this.id = id;
        this.studentId = studentId;
        this.quizId = quizId;
    }

    public int getId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getQuizId() {
        return quizId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int calculateDuration() {
        if (startTime == null || endTime == null) return 0;
        long diffMillis = endTime.getTime() - startTime.getTime();
        return (int) (diffMillis / (1000 * 60)); // duration in minutes
    }
}
