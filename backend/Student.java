
package backend;


import java.util.Date;
import java.util.List;

public class Student extends User {
    private String schoolName;
    private String studentId;
    private List<Quiz> quizzes;
    


    
    public Student() {
        super();
    }
    
    public Student(String name, String surname, int age, String email, String password, List<Annoucement> annoucements, String username, String schoolName, String studentId, List<Quiz> quizzes, Date createdDate, Date updatedDate, boolean isActive) {
        super(name, surname, age, email, password, annoucements, username, createdDate, updatedDate, isActive);
        this.schoolName = schoolName;
        this.studentId = studentId;
        this.quizzes = quizzes;
    }
    
    public String getSchoolName(){
        return schoolName;
    }
    public String getStudentId(){
        return studentId;
    }
    public List<Quiz> getQuizzes(){
        return quizzes;
    }
    public void setSchoolName(String schoolName){
        this.schoolName = schoolName;
    }
    public void setStudentId(String studentId){
        this.studentId = studentId;
    }
    public void setQuizzes(List<Quiz> quizzes){
        this.quizzes = quizzes;
    }
    public int CalculateAverage(){
        int sum = 0;
        for(Quiz quiz : quizzes){
            sum += quiz.getGrade();
        }
        return sum / quizzes.size();
    }
    public int AskAItoGradeQuiz(Quiz quiz){
        return quiz.getGrade();
    }   
    public void ShowUserDetails(){
        System.out.println("Name: " + getName());
        System.out.println("Surname: " + getSurname());
        System.out.println("Age: " + getAge());
        System.out.println("Email: " + getEmail());
        System.out.println("Password: " + getPassword());
        System.out.println("Username: " + getUsername());
        System.out.println("School Name: " + getSchoolName());
        System.out.println("Student ID: " + getStudentId());
        System.out.println("Quizzes: " + getQuizzes());
    }
    public void signIn(User user){
        this.id = user.getId();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.age = user.getAge();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.username = user.getUsername();
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();
    }
    public void logIn(User user){
        this.id = user.getId();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.age = user.getAge();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.username = user.getUsername();
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();
    }
    public void logOut(User user){
        //çıkmak istermisin diye kontrol edicek sonra ana sayfaya yönlendiricek
    }
}