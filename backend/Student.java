public class Student extends User {
    private String schoolName;
    private String studentId;
    private Map<Integer, List<Object>> quizzesAndAnswers;

    Student() {
        super();
        this.schoolName = "";
        this.studentId = "";
        this.quizzesAndAnswers = new HashMap<>();
    }
    
    public Student( String name, String surname, int age, String email, String password, List<String> announcements, String schoolName, String studentId, Map<Integer, List<String>> quizzesAndAnswers, Date createDate, Date updateDate) {
        
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.email = email;
        this.password = password;
        this.announcements = announcements;
        this.schoolName = schoolName;
        this.studentId = studentId;
        this.quizzesAndAnswers = quizzesAndAnswers;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    public String getSchoolName() { return schoolName; }
    public String getStudentId() { return studentId; }
    public Map<Integer, List<Object>> getQuizzesAndAnswers() { return quizzesAndAnswers; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setQuizzesAndAnswers(Map<Integer, List<Object>> quizzesAndAnswers) {
        this.quizzesAndAnswers = quizzesAndAnswers;
    }
    
    public void showUserDetails() { }
    
    public int calculateAverage() {
        // Ortalama hesaplama implementasyonu
        return 0;
    }
    public int AskAItoGradeQuiz(Quiz quiz) {
        // Quiz'i AI'ye gönderme ve puanlama implementasyonu
        return 0;
    }
    public void SignIn(User user) {
        // Kullanıcı girişi implementasyonu
    }
}