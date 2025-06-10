package com.example.QuizSystemProject.dto;

import java.util.List;


public class AuthResponseDto {

    private int userId;
    private String username;
    private List<String> roles;
    private String token;
    private String refreshToken;
    private UserDto user;

    public AuthResponseDto(int userId, String username, List<String> roles, String token, String refreshToken,
            UserDto user) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public AuthResponseDto(int userId, String username, List<String> roles, String token, String refreshToken) {
        this(userId, username, roles, token, refreshToken, null);
    }

    public AuthResponseDto(int userId, String username, List<String> roles, String token) {
        this(userId, username, roles, token, null);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}