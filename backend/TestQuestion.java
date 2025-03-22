public class TestQuestion extends Question {
    private List<String> options;
    public TestQuestion(List<String> options) {
        this.options = options;
    }
    TestQuestion(String question_sentence, List<String> options) {
        this.question_sentence = question_sentence;
        this.options = options;
    }
    private List<String> getOptions() { return options; }
    private void setOptions(List<String> options) { this.options = options; }
   
    
    public String askAIToAnswerQuestion(String question_sentence, List<String> options) { return ""; }

} 
