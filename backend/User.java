public abstract class User {
    protected int id;
    protected String name;
    protected String surname;
    protected int age;
    protected String email;
    protected String password;
    protected List<String> announcements;
    protected Date createDate;
    protected Date updateDate;


    User() {
        super();
    }
    User(String name, String surname, int age, String email, String password, List<String> announcements, Date createdate, Date updatedate) {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.email = email; 
        this.password = password;   
        this.announcements = announcements;
        this.createDate = createdate;
        this.updateDate = updatedate;
    }


    public int getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public int getAge() { return age; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public List<String> getAnnouncements() { return announcements; }
    public Date getCreateDate() { return createDate; }
    public Date getUpdateDate() { return updateDate; }

    // Setter metodları
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setAge(int age) { this.age = age; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setAnnouncements(List<String> announcements) { this.announcements = announcements; }
    public void setCreateDate(Date createDate) { this.createDate = createDate; }
    public void setUpdateDate(Date updateDate) { this.updateDate = updateDate; }

    public void ChangePassword(String newPassword) { this.password = newPassword; }
    public void ChangeEmail(String newEmail) { this.email = newEmail; }
    public void ShowUserDetails() { }
    public void SignIn() { }
    public void LogIn() { }
    public void deleteUser() { }
} 