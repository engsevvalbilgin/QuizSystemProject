package com.example.QuizSystemProject.Controller;

import com.example.QuizSystemProject.Service.MailService;
import com.example.QuizSystemProject.dto.EmailRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @PostMapping("/send-verification")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> sendVerificationEmail(@RequestBody EmailRequestDto emailRequest) {
        try {
            String verificationLink = emailRequest.getVerificationLink();
            if (verificationLink == null || verificationLink.isEmpty()) {
                return ResponseEntity.badRequest().body("Doğrulama linki gereklidir");
            }

            mailService.sendVerificationMail(emailRequest.getEmail(), verificationLink);
            return ResponseEntity.ok("Doğrulama e-postası başarıyla gönderildi");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("E-posta gönderme hatası: " + e.getMessage());
        }
    }

    @PostMapping("/admin/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendCustomEmail(@RequestBody EmailRequestDto emailRequest) {
        try {
            mailService.sendEmail(emailRequest.getEmail(),
                    "Quiz Sistemi Bildirimi",
                    "Bu bir admin tarafından gönderilen özel bildirimdir.");
            return ResponseEntity.ok("E-posta başarıyla gönderildi");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("E-posta gönderme hatası: " + e.getMessage());
        }
    }

    @PostMapping("/admin/send-bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendBulkEmail(@RequestBody EmailRequestDto[] emailRequests) {
        try {
            for (EmailRequestDto request : emailRequests) {
                mailService.sendEmail(request.getEmail(),
                        "Quiz Sistemi Toplu Bildirimi",
                        "Bu bir admin tarafından gönderilen toplu bildirimdir.");
            }
            return ResponseEntity.ok("Toplu e-postalar başarıyla gönderildi");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Toplu e-posta gönderme hatası: " + e.getMessage());
        }
    }
}
