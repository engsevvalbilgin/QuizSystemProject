package com.example.QuizSystemProject;

import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Model.Teacher;
import com.example.QuizSystemProject.Model.QuestionType;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.Repository.QuestionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder importu

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

/**
 * QuizSystemProjectApplication
 */
@SpringBootApplication
public class QuizSystemProjectApplication {

    // PasswordEncoder'ı inject etmek için alan
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Repository'leri inject etmek için alan
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private QuestionTypeRepository questionTypeRepository;
    
    public static void main(String[] args) {
        SpringApplication.run(QuizSystemProjectApplication.class, args);
    }
    
    /**
     * Uygulama başlatıldığında varsayılan soru tiplerini ekler
     */
    @Bean
    public CommandLineRunner initQuestionTypes() {
        return args -> {
            // Soru tiplerini kontrol et, yoksa ekle
            if (questionTypeRepository.count() == 0) {
                System.out.println("Varsayılan soru tipleri oluşturuluyor...");
                
                // Çoktan seçmeli soru tipi (ID: 1) - Frontend'de de 1 olarak kullanılıyor
                QuestionType multipleChoice = new QuestionType();
                multipleChoice.setId(1); // ID'yi manuel olarak ayarlıyoruz
                multipleChoice.setTypeName("Çoktan Seçmeli");
                questionTypeRepository.save(multipleChoice);
                
                // Açık uçlu soru tipi (ID: 2) - Frontend'de de 2 olarak kullanılıyor
                QuestionType openEnded = new QuestionType();
                openEnded.setId(2); // ID'yi manuel olarak ayarlıyoruz
                openEnded.setTypeName("Açık Uçlu");
                questionTypeRepository.save(openEnded);
                
                System.out.println("Soru tipleri başarıyla oluşturuldu.");
            } else {
                System.out.println("Soru tipleri zaten mevcut.");
            }
        };
    }


    @Bean
    public CommandLineRunner demoUser() { // UserRepository parametresini kaldırdık, yukarıda inject ediliyor
        return (args) -> {
   
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
            User adminUser = new User("Admin", "Soyadi", 45, "admin@example.com", "adminuser", 
                passwordEncoder.encode("adminpassword"), "ROLE_ADMIN");
            adminUser.setEnabled(true);
            adminUser.setActive(true);
            adminUser.setCreatedDate(LocalDateTime.now());
            adminUser.setUpdatedDate(LocalDateTime.now());
            userRepository.save(adminUser);
            System.out.println("Kaydedilen Admin: " + adminUser.getUsername());


            // Öğrenci Kullanıcısı
            User studentUser = new User("Ogrenci", "Soyadi", 20, "student@example.com", 
                "studentuser", passwordEncoder.encode("studentpassword"), "ROLE_STUDENT");
            studentUser.setEnabled(false); // Email doğrulama için başlangıçta etkin değil
            studentUser.setActive(true);
            studentUser.setCreatedDate(LocalDateTime.now());
            studentUser.setUpdatedDate(LocalDateTime.now());
            userRepository.save(studentUser);
            System.out.println("Kaydedilen Öğrenci: " + studentUser.getUsername());


            // Öğretmen Kullanıcısı
            Teacher teacherUser = new Teacher();
            teacherUser.setName("Ogretmen");
            teacherUser.setSurname("Soyadi");
            teacherUser.setAge(35);
            teacherUser.setEmail("teacher@example.com");
            teacherUser.setUsername("teacheruser");
            teacherUser.setPassword(passwordEncoder.encode("teacherpassword"));
            teacherUser.setRole("ROLE_TEACHER");
            teacherUser.setEnabled(true);
            teacherUser.setActive(true);
            teacherUser.setCreatedDate(LocalDateTime.now());
            teacherUser.setUpdatedDate(LocalDateTime.now());
            teacherUser.setSubject("Matematik");
            teacherUser.setGraduateSchool("Ankara Üniversitesi");
            teacherUser.setDiplomaNumber("T12345");
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