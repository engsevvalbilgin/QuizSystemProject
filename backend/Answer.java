package com.quizsystem.model;

import java.util.ArrayList;
import java.util.List;

public class Answer {
    private int id;
    private String text;
    private boolean isCorrect;
    private Question question;
    private QuizResult quizResult;
    private List<Option> selectedOptions;
    
    public Answer() {
        this.selectedOptions = new ArrayList<>();
    }
    
    public Answer(int id, String text, boolean isCorrect, Question question, QuizResult quizResult) {
        this.id = id;
        this.text = text;
        this.isCorrect = isCorrect;
        this.question = question;
        this.quizResult = quizResult;
        this.selectedOptions = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public boolean isCorrect() {
        return isCorrect;
    }
    
    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
    
    public Question getQuestion() {
        return question;
    }
    
    public void setQuestion(Question question) {
        this.question = question;
    }
    
    public QuizResult getQuizResult() {
        return quizResult;
    }
    
    public void setQuizResult(QuizResult quizResult) {
        this.quizResult = quizResult;
    }
    
    public List<Option> getSelectedOptions() {
        return selectedOptions;
    }
    
    public void setSelectedOptions(List<Option> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }
    
    /**
     * Adds a selected option to the answer
     * @param option The option to be added to the selected options
     */
    public void addSelectedOption(Option option) {
        this.selectedOptions.add(option);
    }
    
    /**
     * Removes a selected option from the answer
     * @param option The option to be removed from the selected options
     */
    public void removeSelectedOption(Option option) {
        this.selectedOptions.remove(option);
    }
    
    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", isCorrect=" + isCorrect +
                ", question=" + question +
                '}';
    }
}
