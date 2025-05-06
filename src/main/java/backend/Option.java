package backend;
public class Option {
    private int id;
    private int questionId;
    private String text;
    private boolean isCorrect;

    public Option() {}

    public Option(int id, int questionId, String text, boolean isCorrect) {
        this.id = id;
        this.questionId = questionId;
        this.text = text;
        this.isCorrect = isCorrect;
    }

    public int getId() {
        return id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
    public Option createOption(int questionId,String text, boolean isCorrect){
    return new Option();
    }
    public void deleteOption(Option option){}
    public Option updateOption(Option option){
    return new Option();}
    
}
