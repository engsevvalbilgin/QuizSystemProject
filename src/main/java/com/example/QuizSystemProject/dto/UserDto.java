package com.example.QuizSystemProject.dto;

import com.example.QuizSystemProject.Model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private int id;
    private String name;
    private String surname;
    private int age;
    private String email;
    private String username;
    private String role;
    private boolean enabled;
    private String pendingEmail;

    public static UserDto fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getAge(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                user.isEnabled(),
                user.getPendingEmail());
    }
}