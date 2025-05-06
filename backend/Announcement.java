package com.quizsystem.model;

import java.util.Date;

public class Announcement {
    private int id;
    private String title;
    private String content;
    private Date publishDate;
    private Date expiryDate;
    private User publisher;
    private Course course;
    
    public Announcement() {
    }
    
    public Announcement(int id, String title, String content, Date publishDate, Date expiryDate, User publisher, Course course) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.publishDate = publishDate;
        this.expiryDate = expiryDate;
        this.publisher = publisher;
        this.course = course;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Date getPublishDate() {
        return publishDate;
    }
    
    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }
    
    public Date getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public User getPublisher() {
        return publisher;
    }
    
    public void setPublisher(User publisher) {
        this.publisher = publisher;
    }
    
    public Course getCourse() {
        return course;
    }
    
    public void setCourse(Course course) {
        this.course = course;
    }
    
    /**
     * Checks if the announcement is active (current date is between publish date and expiry date)
     * @return true if the announcement is active, false otherwise
     */
    public boolean isActive() {
        Date currentDate = new Date();
        return currentDate.after(publishDate) && currentDate.before(expiryDate);
    }
    
    /**
     * Publishes the announcement by setting the publish date to the current date
     */
    public void publish() {
        this.publishDate = new Date();
    }
    
    /**
     * Updates the announcement content
     * @param title The new title
     * @param content The new content
     */
    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    @Override
    public String toString() {
        return "Announcement{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", publishDate=" + publishDate +
                ", expiryDate=" + expiryDate +
                ", publisher=" + publisher +
                ", course=" + course +
                '}';
    }
}
