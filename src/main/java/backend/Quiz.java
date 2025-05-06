package backend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Quiz {
    private int id;
    private int teacherId;
    private String name;
    private List<Question> questions;
    private List<QuestionAnswer> answers;
    private Date startDate;
    private Date endDate;
    private int duration; // in minutes
    private boolean isActive;
    
    
    
    public Quiz() {
        this.questions = new ArrayList<>();
        this.isActive=true;
    }
    
    public Quiz(int teacherId, String Name,List<Question> questions, Date startDate, Date endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
//        this.duration = startDate.to-endDate;
        this.isActive = true;
        this.questions = new ArrayList<>();
        this.answers=new ArrayList<>();
    }
    public Quiz(int teacherId, String Name,List<Question> questions,List<QuestionAnswer> answers, Date startDate, Date endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
//        this.duration = startDate.to-endDate;
        this.isActive = true;
        this.questions = new ArrayList<>();
        this.answers=new ArrayList<>();
    }
    public Quiz(int teacherId, String Name, Date startDate, Date endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
//        this.duration = startDate.to-endDate;
        this.isActive = true;
        this.questions = new ArrayList<>();
        this.answers=new ArrayList<>();
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<QuestionAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuestionAnswer> answers) {
        this.answers = answers;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public void createQuiz(Quiz quiz) {

    }
   
    public void deleteQuiz(Quiz quiz) {
        
    }
    public void updateQuiz(Quiz quiz) {
    }
    
    public void  showQuizStatistics(){}
    public void  showQuizAnswers(){}
    public void askAiToAnswerQuiz(){}
    public void showQuiz(){}
    public void addQuestion(Question q) {
    }

    public void removeQuestion(Question q) {
    }

    public void updateQuestion(Question q) {
    }
   
    
   
}
