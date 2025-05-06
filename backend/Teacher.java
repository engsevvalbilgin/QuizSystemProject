/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package backend;

/**
 *
 * @author hakan
 */

import java.util.Date;
import java.util.List;

public class Teacher extends User {
    private List<Quiz> quizzes;
    private String subject;
    private String graduateSchool;
    private String diplomaNumber;
    
    public Teacher() {
        super();
        
    }
    
    public Teacher(String name, String surname, int age, String email, String password, List<Annoucement> annoucements, String username, Date createdate, Date updatedate,boolean isActive, List<Quiz> quizzes, String subject, String graduateSchool, String diplomaNumber) {
        super(name, surname, age, email, password, annoucements, username, createdate, updatedate,isActive);
        this.quizzes = quizzes;
        this.subject = subject;
        this.graduateSchool = graduateSchool;
        this.diplomaNumber = diplomaNumber;
    }
    public List<Quiz> getQuizzes(){
        return quizzes;
    }
    public String getSubject(){
        return subject;
    }
    public String getGraduateSchool(){
        return graduateSchool;
    }
    public String getDiploma(){
        return diplomaNumber;
    }
    public void setQuizzes(List<Quiz> quizzes){
        this.quizzes = quizzes;
    }
    public void setSubject(String subject){
        this.subject = subject;
    }
    public void setGraduateSchool(String graduateSchool){
        this.graduateSchool = graduateSchool;
    }
    public void setDiploma(String diplomaNumber){
        this.diplomaNumber = diplomaNumber;
    }
    public void createQuiz(Quiz quiz){
        quizzes.add(quiz);
    }
    public void setTime(Duration time){
        quiz.setTime(time);
    }
    public void showQuizzes(){
        for(Quiz quiz : quizzes){
            System.out.println(quiz);
        }
    }
    public void showUserDetails(){
        System.out.println("Name: " + getName());
        System.out.println("Surname: " + getSurname());
        System.out.println("Age: " + getAge());
        System.out.println("Email: " + getEmail());
        System.out.println("Password: " + getPassword());
        System.out.println("Username: " + getUsername());
        System.out.println("Quizzes: " + getQuizzes());
        System.out.println("Subject: " + getSubject());
        System.out.println("Graduate School: " + getGraduateSchool());
        System.out.println("Diploma: " + getDiploma());
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
