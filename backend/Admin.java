package com.quizsystem.model;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private String adminId;
    private List<User> managedUsers;
    private List<Course> managedCourses;
    
    public Admin() {
        super();
        this.managedUsers = new ArrayList<>();
        this.managedCourses = new ArrayList<>();
    }
    
    public Admin(int id, String name, String surname, int age, String email, String password, String username, String adminId) {
        super(id, name, surname, age, email, password, username);
        this.adminId = adminId;
        this.managedUsers = new ArrayList<>();
        this.managedCourses = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getAdminId() {
        return adminId;
    }
    
    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
    
    public List<User> getManagedUsers() {
        return managedUsers;
    }
    
    public void setManagedUsers(List<User> managedUsers) {
        this.managedUsers = managedUsers;
    }
    
    public List<Course> getManagedCourses() {
        return managedCourses;
    }
    
    public void setManagedCourses(List<Course> managedCourses) {
        this.managedCourses = managedCourses;
    }
    
    /**
     * Creates a new user in the system
     * @param user The user to be created
     * @return The created user
     */
    public User createUser(User user) {
        // Add the user to the list of managed users
        this.managedUsers.add(user);
        return user;
    }
    
    /**
     * Updates an existing user's information
     * @param user The user to be updated
     * @return The updated user
     */
    public User updateUser(User user) {
        // Find the user in the list and update it
        // This is a simplified implementation
        for (int i = 0; i < managedUsers.size(); i++) {
            if (managedUsers.get(i).getId() == user.getId()) {
                managedUsers.set(i, user);
                break;
            }
        }
        return user;
    }
    
    /**
     * Deletes a user from the system
     * @param user The user to be deleted
     */
    public void deleteUser(User user) {
        // Remove the user from the list of managed users
        this.managedUsers.remove(user);
    }
    
    /**
     * Creates a new course in the system
     * @param course The course to be created
     * @return The created course
     */
    public Course createCourse(Course course) {
        // Add the course to the list of managed courses
        this.managedCourses.add(course);
        return course;
    }
    
    /**
     * Updates an existing course's information
     * @param course The course to be updated
     * @return The updated course
     */
    public Course updateCourse(Course course) {
        // Find the course in the list and update it
        // This is a simplified implementation
        for (int i = 0; i < managedCourses.size(); i++) {
            if (managedCourses.get(i).getId() == course.getId()) {
                managedCourses.set(i, course);
                break;
            }
        }
        return course;
    }
    
    /**
     * Deletes a course from the system
     * @param course The course to be deleted
     */
    public void deleteCourse(Course course) {
        // Remove the course from the list of managed courses
        this.managedCourses.remove(course);
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", surname='" + getSurname() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", adminId='" + adminId + '\'' +
                '}';
    }
}
