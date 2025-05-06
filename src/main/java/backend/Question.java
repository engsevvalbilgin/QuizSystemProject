/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package backend;

/**
 *
 * @author hp
 */
public class Question {
    
    protected int id;
    protected int number;
    protected int quizId;
    protected String questionSentence;
    protected QuestionAnswer answer;
    protected QuestionType type;

    public Question(int quizId, QuestionType type) {
        // Constructor body left empty
    }

    public Question(int quizId, QuestionType type, String question_sentence) {
        // Constructor body left empty
    }

    public Question(int quizId, QuestionType type, String question_sentence, QuestionAnswer answer) {
        // Constructor body left empty
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

    public String askAIToAnswerQuestion(String questionSentence){
    return " ";
    }
    public static void createQuestion(){}
    public void deleteQuestion(Question q){}
    public void updateQuestion(Question q){}
}


