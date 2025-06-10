package com.example.QuizSystemProject.Service; 


import org.springframework.beans.factory.annotation.Value; 


import org.springframework.mail.MailException; 
import org.springframework.mail.SimpleMailMessage; 
import org.springframework.mail.javamail.JavaMailSender; 
import org.springframework.stereotype.Service; 

@Service 
public class MailService {

    private final JavaMailSender mailSender; 

    
    @Value("${spring.mail.username}") 
    private String senderEmail; 


    
    
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    


    
    public void sendEmail(String toEmail, String subject, String body) throws MailException { 
        if (toEmail == null || toEmail.trim().isEmpty()) {
            System.err.println("MailService: E-posta gönderme hatası -> 'toEmail' adresi boş olamaz.");
            throw new IllegalArgumentException("'toEmail' adresi boş olamaz.");
        }
        System.out.println("MailService: E-posta gönderme başlatıldı -> Kime: " + toEmail + ", Konu: " + subject);

        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setFrom(senderEmail); 
        message.setTo(toEmail); 
        message.setSubject(subject); 
        message.setText(body); 

        try {
            mailSender.send(message); 
            System.out.println("MailService: E-posta başarıyla gönderildi -> Kime: " + toEmail);
        } catch (MailException e) {
            System.err.println("MailService: E-posta gönderme hatası -> Kime: " + toEmail + ", Hata: " + e.getMessage());
            
            throw e; 
        }
    }

    
    public void sendVerificationMail(String toEmail, String verificationLink) throws MailException { 
        System.out.println("MailService: E-posta dogrulama maili gonderiliyor -> Kime: " + toEmail);

        String subject = "Hesabınızı Dogrulayin";
        
        String body = "Merhaba,\n\nQuiz sistemine kaydınız tamamlanmak üzere. Hesabınızı etkinleştirmek için lütfen aşağıdaki linke tıklayın:\n"
                    + verificationLink
                    + "\n\nBu maili siz talep etmediyseniz lütfen dikkate almayın.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";

        sendEmail(toEmail, subject, body); 
    }

    
    public void sendPasswordResetMail(String toEmail, String resetLink) throws MailException { 
        System.out.println("MailService: Parola sifirlama maili gonderiliyor -> Kime: " + toEmail);

        String subject = "Parola Sıfırlama Talebi";
        
        String body = "Merhaba,\n\nParola sıfırlama talebinde bulundunuz. Parolanızı sıfırlamak için lütfen aşağıdaki linke tıklayın:\n"
                    + resetLink
                    + "\n\nBu maili siz talep etmediyseniz lütfen dikkate almayın veya bizimle iletişime geçin.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";

        sendEmail(toEmail, subject, body); 
    }
    
 
    public void sendPasswordResetSuccessMail(String toEmail) throws MailException {
        System.out.println("MailService: Parola sifirlama basari maili gonderiliyor -> Kime: " + toEmail);

        String subject = "Parolanız Başarıyla Sıfırlandı";
        String body = "Merhaba,\n\nQuiz sistemi parolanız başarıyla sıfırlandı.\n\nEğer parolanızı siz sıfırlamadıysanız, lütfen hemen hesabınızı kontrol edin veya bizimle iletişime geçin.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";

        sendEmail(toEmail, subject, body); 
    }
    
    
    public void sendPasswordChangeNotification(String toEmail) throws MailException {
        System.out.println("MailService: Şifre değişimi bildirim maili gönderiliyor -> Kime: " + toEmail);

        String subject = "Şifreniz Değiştirildi";
        String body = "Merhaba,\n\nQuiz sistemindeki hesabınızın şifresi başarıyla değiştirildi.\n\nEğer bu işlemi siz yapmadıysanız, lütfen derhal bizimle iletişime geçin.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";

        sendEmail(toEmail, subject, body);
    }
    
    
    public void sendEmailChangeVerification(String toEmail, String verificationToken, String baseUrl) throws MailException {
        System.out.println("MailService: E-posta değişikliği doğrulama maili gönderiliyor -> Kime: " + toEmail);

        String verificationLink = baseUrl + "/verify-email?token=" + verificationToken;
        System.out.println("MailService: E-posta doğrulama linki oluşturuldu: " + verificationLink);
        
        String subject = "E-posta Adresinizi Değiştirme Talebi";
        String body = "Merhaba,\n\nQuiz sistemindeki hesabınızın e-posta adresini değiştirme talebinde bulundunuz.\n\nYeni e-posta adresinizi doğrulamak için lütfen aşağıdaki bağlantıya tıklayın:\n" 
                + verificationLink
                + "\n\nEğer bu talebi siz yapmadıysanız, güvenliğiniz için lütfen bu bağlantıyı tıklamayın ve bizimle iletişime geçin.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";

        sendEmail(toEmail, subject, body);
    }

    
    
}
