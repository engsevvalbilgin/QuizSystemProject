public class Quiz {
    private int id;
    private String name;
    private List<Question> questions;
    private int time;
    private int teacherId;
    private List<String> answers;

    Quiz() {        
        super();
    }
    Quiz(int teacherId, String name, List<Question> questions, int time) {
        this.teacherId = teacherId;
        this.name = name;
        this.questions = questions;
        this.time = time;
    }
    Quiz(int teacherId, String name, List<Question> questions, List<String> answers, int time) {
        this.teacherId = teacherId;
        this.name = name;
        this.questions = questions;
        this.answers = answers;
        this.time = time;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public List<Question> getQuestions() { return questions; }
    public int getTime() { return time; }
    public int getTeacherId() { return teacherId; }
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public void setTime(int time) { this.time = time; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public void ShowQuizStatistics() { }
    public void ShowQuizAnswers() { }
    public void AskAiToAnswerQuiz() { }
 
} 