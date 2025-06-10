package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Service.UserService;
import com.example.QuizSystemProject.dto.TeacherRegistrationRequest;
import com.example.QuizSystemProject.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teachers")
public class TeacherRegistrationController {

    private final UserService userService;

    public TeacherRegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerTeacher(
            @Valid @RequestBody TeacherRegistrationRequest registrationRequest) {
        System.out.println("TeacherRegistrationController: Öğretmen kayıt isteği alındı - Email: "
                + registrationRequest.getEmail());

        User registeredTeacher = userService.submitTeacherRegistration(registrationRequest);

        UserResponse userResponse = new UserResponse(registeredTeacher);

        System.out.println(
                "TeacherRegistrationController: Öğretmen kayıt başarılı - Kullanıcı ID: " + registeredTeacher.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
