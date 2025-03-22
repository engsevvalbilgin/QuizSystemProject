public abstract class Question {
    private int id;
    private int number;
    private int quizId;
    private String questionSentence;
    private String answer;

    public Question(int id, int number, int quizId, String questionSentence, String answer) {
        this.id = id;
        this.number = number;
        this.quizId = quizId;
        this.questionSentence = questionSentence;
        this.answer = answer;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public String getQuestionSentence() { return questionSentence; }
    public void setQuestionSentence(String questionSentence) { this.questionSentence = questionSentence; }
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public String askAIToAnswerQuestion(String question_sentence) { return ""; }    
} 