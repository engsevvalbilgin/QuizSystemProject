package com.example.QuizSystemProject.Service; // Paket adınızın doğru olduğundan emin olun


import org.springframework.beans.factory.annotation.Value; // application.properties'ten değer okumak için <-- BU IMPORTU EKLEYİN


import org.springframework.mail.MailException; // Mail gönderme hataları için <-- BU IMPORTU EKLEYİN
import org.springframework.mail.SimpleMailMessage; // Basit metin e-postalar için
import org.springframework.mail.javamail.JavaMailSender; // Spring'in e-posta gönderme bileşeni
import org.springframework.stereotype.Service; // Service anotasyonunu import edin

@Service // Spring'e bu sınıfın bir Service bileşeni olduğunu belirtir
public class MailService {

    private final JavaMailSender mailSender; // Spring'in e-posta gönderme bileşeni

    // Mail'i gönderen adresi application.properties'ten okumak için
    // spring.mail.username ayarından otomatik olarak alınır.
    @Value("${spring.mail.username}") // <-- application.properties'teki spring.mail.username değerini bu alana inject et
    private String senderEmail; // <-- Gönderen mail adresi


    // JavaMailSender bağımlılığının enjekte edildiği constructor
    
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    


    // --- Sizin MailService template'inizdeki işlevlere karşılık gelen metot imzaları ---

    // Genel amaçlı e-posta gönderme metodu - ŞİMDİ GERÇEK GÖNDERME YAPACAK
    // Sizin template'inizdeki sendMail metoduna karşılık gelir
    public void sendEmail(String toEmail, String subject, String body) throws MailException { // <-- throws MailException eklendi
        if (toEmail == null || toEmail.trim().isEmpty()) {
            System.err.println("MailService: E-posta gönderme hatası -> 'toEmail' adresi boş olamaz.");
            throw new IllegalArgumentException("'toEmail' adresi boş olamaz.");
        }
        System.out.println("MailService: E-posta gönderme başlatıldı -> Kime: " + toEmail + ", Konu: " + subject);

        SimpleMailMessage message = new SimpleMailMessage(); // Basit metin tabanlı mail objesi
        message.setFrom(senderEmail); // <-- Kimden gönderildiği (application.properties'ten alınan username)
        message.setTo(toEmail); // <-- Kime gönderildiği
        message.setSubject(subject); // <-- Konu
        message.setText(body); // <-- Mail içeriği (sadece metin)

        try {
            mailSender.send(message); // <-- GERÇEK MAİL GÖNDERME METODU AKTİF EDİLDİ
            System.out.println("MailService: E-posta başarıyla gönderildi -> Kime: " + toEmail);
        } catch (MailException e) {
            System.err.println("MailService: E-posta gönderme hatası -> Kime: " + toEmail + ", Hata: " + e.getMessage());
            // Hatanın yayılmasına izin ver (çağıran metot yakalayabilir)
            throw e; // <-- Hatayı tekrar fırlat
        }
    }

    // E-posta doğrulama maili gönderme metodu - ŞİMDİ GERÇEK GÖNDERME YAPACAK
    // Sizin template'inizdeki sendVerificationMail metoduna karşılık gelir
    // Bu metot genel sendEmail metodunu kullanacak.
    public void sendVerificationMail(String toEmail, String verificationLink) throws MailException { // <-- throws MailException eklendi
        System.out.println("MailService: E-posta dogrulama maili gonderiliyor -> Kime: " + toEmail);

        String subject = "Hesabınızı Dogrulayin";
        // Mail içeriğini biraz daha bilgilendirici yapalım
        String body = "Merhaba,\n\nQuiz sistemine kaydınız tamamlanmak üzere. Hesabınızı etkinleştirmek için lütfen aşağıdaki linke tıklayın:\n"
                    + verificationLink
                    + "\n\nBu maili siz talep etmediyseniz lütfen dikkate almayın.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";

        sendEmail(toEmail, subject, body); // Genel gönderme metodunu kullan
    }

    // Parola sıfırlama maili gönderme metodu - ŞİMDİ GERÇEK GÖNDERME YAPACAK
    // Sizin template'inizdeki sendPasswordMail metoduna karşılık gelir
    // Bu metot genel sendEmail metodunu kullanacak.
    public void sendPasswordResetMail(String toEmail, String resetLink) throws MailException { // <-- throws MailException eklendi
        System.out.println("MailService: Parola sifirlama maili gonderiliyor -> Kime: " + toEmail);

        String subject = "Parola Sıfırlama Talebi";
        // Mail içeriğini biraz daha bilgilendirici yapalım
        String body = "Merhaba,\n\nParola sıfırlama talebinde bulundunuz. Parolanızı sıfırlamak için lütfen aşağıdaki linke tıklayın:\n"
                    + resetLink
                    + "\n\nBu maili siz talep etmediyseniz lütfen dikkate almayın veya bizimle iletişime geçin.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";

        sendEmail(toEmail, subject, body); // Genel gönderme metodunu kullan
    }
    
 // Parola sıfırlama başarı maili gönderme metodu
    public void sendPasswordResetSuccessMail(String toEmail) throws MailException {
        System.out.println("MailService: Parola sifirlama basari maili gonderiliyor -> Kime: " + toEmail);

        String subject = "Parolanız Başarıyla Sıfırlandı";
        String body = "Merhaba,\n\nQuiz sistemi parolanız başarıyla sıfırlandı.\n\nEğer parolanızı siz sıfırlamadıysanız, lütfen hemen hesabınızı kontrol edin veya bizimle iletişime geçin.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";

        sendEmail(toEmail, subject, body); // Genel gönderme metodunu kullan
    }
    
    // Şifre değişimi bildirim maili gönderme metodu
    public void sendPasswordChangeNotification(String toEmail) throws MailException {
        System.out.println("MailService: Şifre değişimi bildirim maili gönderiliyor -> Kime: " + toEmail);

        String subject = "Şifreniz Değiştirildi";
        String body = "Merhaba,\n\nQuiz sistemindeki hesabınızın şifresi başarıyla değiştirildi.\n\nEğer bu işlemi siz yapmadıysanız, lütfen derhal bizimle iletişime geçin.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";

        sendEmail(toEmail, subject, body);
    }
    
    // E-posta değişikliği doğrulama maili gönderme metodu
    public void sendEmailChangeVerification(String toEmail, String verificationToken, String baseUrl) throws MailException {
        System.out.println("MailService: E-posta değişikliği doğrulama maili gönderiliyor -> Kime: " + toEmail);

        // Use the exact route path that matches the React application's router configuration
        String verificationLink = baseUrl + "/verify-email?token=" + verificationToken;
        System.out.println("MailService: E-posta doğrulama linki oluşturuldu: " + verificationLink);
        
        String subject = "E-posta Adresinizi Değiştirme Talebi";
        String body = "Merhaba,\n\nQuiz sistemindeki hesabınızın e-posta adresini değiştirme talebinde bulundunuz.\n\nYeni e-posta adresinizi doğrulamak için lütfen aşağıdaki bağlantıya tıklayın:\n" 
                + verificationLink
                + "\n\nEğer bu talebi siz yapmadıysanız, güvenliğiniz için lütfen bu bağlantıyı tıklamayın ve bizimle iletişime geçin.\n\nTeşekkürler,\nQuiz Sistemi Ekibi";

        sendEmail(toEmail, subject, body);
    }

    // --- Template'teki statik alanlar (sender_mail_address, sender_password) buraya gelmez.
    // --- Bunlar application.properties dosyasında yapılandırılır. ---
}
