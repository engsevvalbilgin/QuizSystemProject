/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package backend;

/**
 *
 * @author hakan
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User {
    protected int id;
    protected String name;
    protected int age;
    protected String surname;
    protected String email;
    protected String password;
    protected String username;
    protected Date createdDate;
    protected Date updatedDate;
    protected boolean isActive;
    protected List<String> annoucements;
    
    
    
    public User() {
        this.quizzes = new ArrayList<>();
        this.annoucements = new ArrayList<>();
        this.isActive = true;
    }
    
    public User( String name, String surname,int age, String email, String password, String username,Date createdate,Date updatedate, Boolean isActive) {
        
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.email = email;
        this.password = password;
        
        this.username = username;
        this.createdDate = createdate;
        this.updatedDate = updatedate;
        this.isActive = true;
        this.annoucements = new ArrayList<>();
        this.quizzes = new ArrayList<>();
    }

    

        
    
    
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSurname() {
        return surname;
    }
    
    public void setSurname(String surname) {
        this.surname = surname;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getAnnoucements(){
        return annoucements;
    }

    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    public Date getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public String getUsername() {
        return username;
    }
    
    private void setUsername(String username) {
        this.username = username;
    }
   
    public void ChangePassword(String newPassword){
        this.password = newPassword;
    }  

    public void ChangeEmail(String newEmail){
        this.email = newEmail;
    }
    private void setIsActive(boolean newActivity){
        this.isActive = newActivity;
    }
    public boolean getIsActive(){
        return isActive;
    }
    
    public void deleteUser(){
       this.isActive = false;
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

