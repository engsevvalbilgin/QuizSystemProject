
package backend;


public class QuestionAnswer {
    private int id;
    private int takeQuizId;
    private int questionId;
    private String answer;
    private boolean isCorrect;
    

    public QuestionAnswer() {

    }

    public QuestionAnswer(int id, int takeQuizId, int questionId, String answer, boolean isCorrect) {
        this.id = id;
        this.takeQuizId = takeQuizId;
        this.questionId = questionId;
        this.answer = answer;
        this.isCorrect = isCorrect; 

    }

    public int getId() {
        return id;
    }

    public int getTakeQuizId() {
        return takeQuizId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTakeQuizId(int takeQuizId) {
        this.takeQuizId = takeQuizId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
        this.isCorrect = checkAnswer(); 
    }

    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public boolean checkAnswer() {
        return this.answer.equals(this.question.getCorrectAnswer());
    }
}