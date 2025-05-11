package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.Service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailController {

    @Autowired
    private MailService mailService;

    @GetMapping("/send-verification-email")
    public String sendVerificationEmail() {
        try {
            // Burada bir mail adresi ve doğrulama linki sağlanmalıdır
            String toEmail = "test@example.com"; // Test için e-posta adresi
            String verificationLink = "http://example.com/verify?token=12345"; // Test doğrulama linki
            mailService.sendVerificationMail(toEmail, verificationLink);
            return "Doğrulama e-postası başarıyla gönderildi!";
        } catch (Exception e) {
            return "E-posta gönderme hatası: " + e.getMessage();
        }
    }
}
