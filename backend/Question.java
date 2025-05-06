/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.quizprojesi2;

/**
 *
 * @author hp
 */
public abstract class Question {
    
    protected int id;
    protected int number;
    protected int quizId;
    protected String questionSentence;
    protected QuestionAnswer answer;
    protected QuestionType type;

    public Question() {}

    public Question(String questionSentence) {
        this.questionSentence = questionSentence;
    }

    public Question(String questionSentence, int number) {
        this.questionSentence = questionSentence;
        this.number = number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getQuestionSentence() {
        return questionSentence;
    }

    public void setQuestionSentence(String questionSentence) {
        this.questionSentence = questionSentence;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public abstract String askAIToAnswerQuestion(String questionSentence);
}


