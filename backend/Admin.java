public class Admin extends User {

    Admin() {
        super();
    }
    Admin(String name, String surname, int age, String email, String password, List<String> announcements, Date createdate, Date updatedate) {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.email = email;
        this.password = password;
        this.announcements = announcements;
        this.createdate = createdate;
        this.updatedate = updatedate;
    }
    public void AddUser(User user) { }
    public void UpdateUser(User user) { }
    public void ShowAllUsers() { }
    public void ShowAllStudents() { }
    public void ShowAllTeachers() { }
    public boolean ReviewTeacherRequest() { return true; }
    public void ShowTeacherRequests() { }
    public void AddAnnouncement() { }
    public void ShowProgramStatistics() { }
    public void ShowUserDetails() { }
}
