public class Teacher extends User {
    private List<Quiz> quizzes;
    private String subject;
    private String graduateSchool;
    private String diploma;

    Teacher() {
        super();
        this.quizzes = new ArrayList<>();
        this.subject = "";
        this.graduateSchool = "";
        this.diploma = "";
    }
    public Teacher( String name, String surname, int age, String email, String password, List<String> announcements, List<Quiz> quizzes, String subject, String graduateSchool, String diploma) {
        
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.email = email;
        this.password = password;
        this.announcements = announcements;
        this.quizzes = quizzes;
        this.subject = subject;
        this.graduateSchool = graduateSchool;
        this.diploma = diploma;
    }   

    public List<Quiz> getQuizzes() { return quizzes; }
    public String getSubject() { return subject; }
    public String getGraduateSchool() { return graduateSchool; }
    public String getDiploma() { return diploma; }
    public void setQuizzes(List<Quiz> quizzes) { this.quizzes = quizzes; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setGraduateSchool(String graduateSchool) { this.graduateSchool = graduateSchool; }
    public void setDiploma(String diploma) { this.diploma = diploma; }
    
    public void CreateQuiz(Quiz quiz) { }
    public void SetTime(int time) { }
    public void ShowQuizzes() { }
    public void ShowUserDetails() { }
    public void SignIn(User user) { }
}