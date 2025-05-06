package com.quizprojesi2;
import java.util.List;

public class TestQuestion extends Question {
    private List<Option> options;

    public TestQuestion() {}

    public TestQuestion(String questionSentence, List<Option> options) {
        super(questionSentence);
        this.options = options;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> newOptions) {
        this.options = newOptions;
    }

    @Override
    public String askAIToAnswerQuestion(String questionSentence) {
        // Simulated AI answer
        return "AI selected option: " + (options != null && !options.isEmpty() ? options.get(0).getText() : "N/A");
    }

    public String askAIToAnswerQuestion(String questionSentence, List<Option> options) {
        return "AI thinks the best answer is: " + (options != null && !options.isEmpty() ? options.get(0).getText() : "N/A");
    }
}
