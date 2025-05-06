package com.quizsystem.model;

import java.util.ArrayList;
import java.util.List;

public class Teacher extends User {
    private String teacherId;
    private String department;
    private List<Course> courses;
    
    public Teacher() {
        super();
        this.courses = new ArrayList<>();
    }
    
    public Teacher(int id, String name, String surname, int age, String email, String password, String username, String teacherId, String department) {
        super(id, name, surname, age, email, password, username);
        this.teacherId = teacherId;
        this.department = department;
        this.courses = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public List<Course> getCourses() {
        return courses;
    }
    
    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
    
    /**
     * Adds a course to the teacher's list of courses
     * @param course The course to be added
     */
    public void addCourse(Course course) {
        this.courses.add(course);
    }
    
    /**
     * Removes a course from the teacher's list of courses
     * @param course The course to be removed
     */
    public void removeCourse(Course course) {
        this.courses.remove(course);
    }
    
    /**
     * Creates a new quiz for a specific course
     * @param course The course for which the quiz is created
     * @param quiz The quiz to be created
     * @return The created quiz
     */
    public Quiz createQuiz(Course course, Quiz quiz) {
        // Add the quiz to the course
        course.addQuiz(quiz);
        return quiz;
    }
    
    /**
     * Grades a student's quiz result
     * @param quizResult The quiz result to be graded
     * @param score The score to be assigned
     */
    public void gradeQuiz(QuizResult quizResult, double score) {
        // Set the score for the quiz result
        quizResult.setScore(score);
    }
    
    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", surname='" + getSurname() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", teacherId='" + teacherId + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
