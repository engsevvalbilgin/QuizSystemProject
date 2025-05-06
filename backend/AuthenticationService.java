package com.quizsystem.model;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationService {
    private int id;
    private Map<String, User> userCredentials;
    private User currentUser;
    
    public AuthenticationService() {
        this.userCredentials = new HashMap<>();
    }
    
    public AuthenticationService(int id) {
        this.id = id;
        this.userCredentials = new HashMap<>();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Map<String, User> getUserCredentials() {
        return userCredentials;
    }
    
    public void setUserCredentials(Map<String, User> userCredentials) {
        this.userCredentials = userCredentials;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    /**
     * Registers a new user in the system
     * @param user The user to register
     * @return true if registration was successful, false otherwise
     */
    public boolean registerUser(User user) {
        // Check if username already exists
        if (userCredentials.containsKey(user.getUsername())) {
            return false;
        }
        
        // Add user to credentials map
        userCredentials.put(user.getUsername(), user);
        return true;
    }
    
    /**
     * Authenticates a user with username and password
     * @param username The username
     * @param password The password
     * @return The authenticated user or null if authentication failed
     */
    public User login(String username, String password) {
        // Check if username exists
        if (!userCredentials.containsKey(username)) {
            return null;
        }
        
        // Get the user
        User user = userCredentials.get(username);
        
        // Check if password matches
        if (user.getPassword().equals(password)) {
            currentUser = user;
            return user;
        }
        
        return null;
    }
    
    /**
     * Logs out the current user
     */
    public void logout() {
        currentUser = null;
    }
    
    /**
     * Changes the password for a user
     * @param user The user
     * @param oldPassword The old password
     * @param newPassword The new password
     * @return true if password change was successful, false otherwise
     */
    public boolean changePassword(User user, String oldPassword, String newPassword) {
        // Check if old password matches
        if (!user.getPassword().equals(oldPassword)) {
            return false;
        }
        
        // Change password
        user.setPassword(newPassword);
        return true;
    }
    
    /**
     * Checks if a user is authenticated
     * @return true if a user is currently authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    /**
     * Gets the role of the current user
     * @return The role of the current user as a string
     */
    public String getCurrentUserRole() {
        if (currentUser == null) {
            return "Guest";
        }
        
        if (currentUser instanceof Admin) {
            return "Admin";
        } else if (currentUser instanceof Teacher) {
            return "Teacher";
        } else if (currentUser instanceof Student) {
            return "Student";
        } else {
            return "User";
        }
    }
    
    @Override
    public String toString() {
        return "AuthenticationService{" +
                "id=" + id +
                ", userCredentials=" + userCredentials.size() +
                ", currentUser=" + currentUser +
                '}';
    }
}
