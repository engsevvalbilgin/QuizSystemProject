package backend;


import java.util.List;
import java.util.Date;

public class Admin extends User {
    private List<Announcement> announcements;
    
    public Admin() {
        super();
        
    }
    
     public Admin(String name, String surname, int age, String email, String password, 
                 List<Announcement> announcements, String username, 
                 Date createdDate, Date updatedDate, boolean isActive) {
        // User sÄ±nÄ±fÄ±nÄ±n constructor'Ä±nÄ± doÄŸru parametrelerle Ã§aÄŸÄ±rÄ±yoruz
        super(name, surname, age, email, password, announcements, username, createdDate, updatedDate, isActive);
        this.announcements = announcements; // Admin sÄ±nÄ±fÄ±na Ã¶zel announcements alanÄ±nÄ± baÅŸlatÄ±yoruz
    }
    
    public void setAnnouncements(List<Announcement> announcements){
        this.announcements = announcements;
    }
    public void addUser(User user){
        
    }
    public void updateUser(User user){
        
    }
    public void showAllUsers(){
        
    }
    public void showAllStudents(){
        
    }
    public void showAllTeachers(){
        
    }
    public boolean reviewTeacherRequest(){
        return true;
    }
    public void showTeacherRequests(){
        
    }
    public void addAnnouncement(){
        this.announcements.add(new Announcement());
    }
    public void showProgramStatistics(){
       
    }



    public void showUserDetails(){
        System.out.println("Name: " + getName());
        System.out.println("Surname: " + getSurname());
        System.out.println("Age: " + getAge());
        System.out.println("Email: " + getEmail());
        System.out.println("Password: " + getPassword());
        System.out.println("Username: " + getUsername());
        
    }
    @Override
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
    @Override
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
    @Override
    public void logOut(User user){
        //Ã§Ä±kmak istermisin diye kontrol edicek sonra ana sayfaya yÃ¶nlendiricek
    }

}
