package com.example.QuizSystemProject;

import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder importu

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;


@SpringBootApplication
public class QuizSystemProjectApplication {

    // PasswordEncoder'ı inject etmek için alan
    @Autowired
    private PasswordEncoder passwordEncoder;

    // UserRepository'yi inject etmek için alan
    @Autowired
    private UserRepository userRepository;


    public static void main(String[] args) {
        SpringApplication.run(QuizSystemProjectApplication.class, args);
    }


    @Bean
    public CommandLineRunner demoUser() { // UserRepository parametresini kaldırdık, yukarıda inject ediliyor
        return (args) -> {
             // TODO: Bu metodun yalnızca bir kere çalışmasını sağlamak için kontrol ekleyebilirsiniz.
             // Örneğin, eğer Admin kullanıcısı veritabanında yoksa ekle gibi.
             // Basit bir kontrol ekleyelim: Eğer varsayılan kullanıcılardan herhangi biri (örn: admin) zaten varsa, ekleme.
             if (userRepository.findByUsername("adminuser").isPresent()) {
                 System.out.println("\nVarsayılan kullanıcılar zaten mevcut, tekrar eklenmiyor.");
                 // Temel okuma testlerini yine de çalıştırabilirsiniz isterseniz:
                 // runInitialReadTests(); // Eğer bu testleri ayrı bir metoda taşırsanız
                 return; // Kullanıcılar zaten varsa metodu sonlandır
             }

            System.out.println("\nVarsayılan kullanıcılar ekleniyor...");

            // -- Kullanıcı Oluşturma ve Kaydetme (CREATE) --

            // Kullanıcıları oluştururken parolayı passwordEncoder.encode() ile ŞİFRELE!
            // enabled durumunu email akışına uygun ayarla.

            // Admin Kullanıcısı
            User adminUser = new User("Admin", "Soyadi", 45, "admin@example.com", "adminuser", passwordEncoder.encode("adminpassword"), "ROLE_ADMIN"); // <-- Parola şifrelendi
            adminUser.setEnabled(true); // Admin genellikle başlangıçta etkin
            adminUser.setActive(true);
            adminUser.setCreatedDate(LocalDateTime.now());
            adminUser.setUpdatedDate(LocalDateTime.now());
            userRepository.save(adminUser);
            System.out.println("Kaydedilen Admin: " + adminUser.getUsername());


            // Öğrenci Kullanıcısı
            User studentUser = new User("Ogrenci", "Soyadi", 20, "student@example.com", "studentuser", passwordEncoder.encode("studentpassword"), "ROLE_STUDENT"); // <-- Parola şifrelendi
            studentUser.setEnabled(false); // <-- Email doğrulama için başlangıçta etkin değil
            studentUser.setActive(true);
            studentUser.setCreatedDate(LocalDateTime.now());
            studentUser.setUpdatedDate(LocalDateTime.now());
            userRepository.save(studentUser);
            System.out.println("Kaydedilen Öğrenci: " + studentUser.getUsername());


            // Öğretmen Kullanıcısı
            User teacherUser = new User("Ogretmen", "Soyadi", 35, "teacher@example.com", "teacheruser", passwordEncoder.encode("teacherpassword"), "ROLE_TEACHER"); // <-- Parola şifrelendi
            teacherUser.setEnabled(false); // <-- Email doğrulama için başlangıçta etkin değil
            teacherUser.setActive(true);
            teacherUser.setCreatedDate(LocalDateTime.now());
            teacherUser.setUpdatedDate(LocalDateTime.now());
            userRepository.save(teacherUser);
            System.out.println("Kaydedilen Öğretmen: " + teacherUser.getUsername());


            System.out.println("Varsayılan kullanıcı ekleme tamamlandı.");


            // --- Temel CRUD/Okuma testleri (İsteğe bağlı, burada kalabilir veya silinebilir) ---
            // Bu kısım parolaların şifrelenmesiyle ilgili BadCredentialsException'ı etkilemez

            System.out.println("\nBaşlangıç Veri Ekleme ve Temel Okuma Testleri:");

            System.out.println("\nTüm Kullanıcılar:");
            List<User> users = userRepository.findAll(); // Tüm kullanıcıları çekme
            users.forEach(user -> System.out.println(user.getUsername() + " - " + user.getRole() + " - Enabled: " + user.isEnabled())); // Daha sade çıktı

            System.out.println("\nID'si 1 olan Kullanıcı:");
            Optional<User> userById = userRepository.findById(users.stream().filter(u -> u.getUsername().equals("studentuser")).findFirst().get().getId()); // studentuser'in ID'sini dinamik bul
            userById.ifPresent(user -> System.out.println(user.getUsername()));

            System.out.println("\nKullanıcı adı 'teacheruser' olan Kullanıcı:");
            Optional<User> userByUsernameOptional = userRepository.findByUsername("teacheruser");
            userByUsernameOptional.ifPresent(user -> System.out.println(user.getUsername()));

            System.out.println("\nRolü 'ROLE_STUDENT' olan Kullanıcılar:");
            List<User> students = userRepository.findAllByRole("ROLE_STUDENT");
            students.forEach(user -> System.out.println(user.getUsername()));

            System.out.println("\nBaşlangıç testleri tamamlandı.");

        }; // CommandLineRunner body sonu
    } // demoUser Bean metodu sonu

    // İsteğe bağlı: Eğer önceki loglarda gördüğünüz CRUD testlerini ayrı bir metoda taşımak isterseniz buraya ekleyebilirsiniz.
    // private void runInitialReadTests() {
    //    System.out.println("\nTemel Okuma Testleri (Kullanıcılar Mevcutsa):");
    //    // Yukarıdaki okuma testlerini buraya kopyalayın
    // }

    // ... diğer metotlar (varsa) ...
}