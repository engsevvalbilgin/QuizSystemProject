/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package backend;

/**
 *
 * @author hakan
 */

import java.util.List;

public class Admin extends User {
    private List<Annoucement> announcements;
    
    public Admin() {
        super();
        
    }
    
    public Admin(String name, String surname, int age, String email, String password, List<Annoucement> announcements, Date createdate, Date updatedate, String username, boolean isActive) {
        super(name, surname, age, email, password, username, createdate, updatedate, isActive);
        this.announcements = announcements;
    }
    
    public void setAnnouncements(List<Annoucement> announcements){
        this.announcements = announcements;
    }
    public void addUser(User user){
        this.announcements.add(user);
    }
    public void updateUser(User user){
        this.announcements.add(user);
    }
    public void showAllUsers(){
        for(User user : this.announcements){
            System.out.println(user);
        }
    }
    public void showAllStudents(){
        for(User user : this.announcements){
            System.out.println(user);
        }
    }
    public void showAllTeachers(){
        for(User user : this.announcements){
            System.out.println(user);
        }
    }
    public boolean reviewTeacherRequest(){
        return true;
    }
    public void showTeacherRequests(){
        for(User user : this.announcements){
            System.out.println(user);
        }
    }
    public void addAnnouncement(){
        this.announcements.add(new Annoucement());
    }
    public void showProgramStatistics(){
        for(User user : this.announcements){
            System.out.println(user);
        }
    }



    public void showUserDetails(){
        System.out.println("Name: " + getName());
        System.out.println("Surname: " + getSurname());
        System.out.println("Age: " + getAge());
        System.out.println("Email: " + getEmail());
        System.out.println("Password: " + getPassword());
        System.out.println("Username: " + getUsername());
        System.out.println("Announcements: " + getAnnouncements());
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
