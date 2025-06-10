package com.example.QuizSystemProject;

import com.example.QuizSystemProject.Model.User; 
import com.example.QuizSystemProject.Model.Teacher;
import com.example.QuizSystemProject.Model.Student;
import com.example.QuizSystemProject.Model.Admin;
import com.example.QuizSystemProject.Model.QuestionType;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.Repository.QuestionTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn; 
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@SpringBootApplication
public class QuizSystemProjectApplication {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final QuestionTypeRepository questionTypeRepository;

    public QuizSystemProjectApplication(PasswordEncoder passwordEncoder,
                                        UserRepository userRepository,
                                        QuestionTypeRepository questionTypeRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.questionTypeRepository = questionTypeRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(QuizSystemProjectApplication.class, args);
    }

    @Bean
    public CommandLineRunner initQuestionTypes() {
        return args -> {
           
            if (questionTypeRepository.count() == 0) {
                System.out.println("Varsayılan soru tipleri oluşturuluyor...");

                QuestionType multipleChoice = new QuestionType();
                multipleChoice.setId(1); 
                multipleChoice.setTypeName("Çoktan Seçmeli");
                questionTypeRepository.save(multipleChoice);
                QuestionType openEnded = new QuestionType();
                openEnded.setId(2); 
                openEnded.setTypeName("Açık Uçlu");
                questionTypeRepository.save(openEnded);

                System.out.println("Soru tipleri başarıyla oluşturuldu.");
            } else {
                System.out.println("Soru tipleri zaten mevcut.");
            }
        };
    }

    @Bean
    @DependsOn({"entityManagerFactory", "dataSource"})
    public CommandLineRunner demoUser() {
        return (args) -> {

            if (userRepository.findByUsername("adminuser").isPresent()) {
                System.out.println("\nVarsayılan kullanıcılar zaten mevcut, tekrar eklenmiyor.");
                return; 
            }

            System.out.println("\nVarsayılan kullanıcılar ekleniyor...");

            Admin adminUser = new Admin();
            adminUser.setName("Admin");
            adminUser.setSurname("Soyadi");
            adminUser.setAge(45);
            adminUser.setEmail("admin@example.com");
            adminUser.setUsername("adminuser");
            adminUser.setPassword(passwordEncoder.encode("adminpassword")); 
            adminUser.setRole("ROLE_ADMIN");
            adminUser.setEnabled(true);
            adminUser.setActive(true);
            adminUser.setCreatedDate(LocalDateTime.now());
            adminUser.setUpdatedDate(LocalDateTime.now());
            userRepository.save(adminUser);
            System.out.println("Kaydedilen Yönetici: " + adminUser.getUsername());


            Student studentUser = new Student();
            studentUser.setName("Ogrenci");
            studentUser.setSurname("Soyadi");
            studentUser.setAge(20);
            studentUser.setEmail("student@example.com");
            studentUser.setUsername("studentuser");
            studentUser.setPassword(passwordEncoder.encode("studentpassword")); 
            studentUser.setRole("ROLE_STUDENT");
            studentUser.setSchoolName("Default School");
            studentUser.setEnabled(false); 
            studentUser.setActive(true);
            studentUser.setCreatedDate(LocalDateTime.now());
            studentUser.setUpdatedDate(LocalDateTime.now());
            userRepository.save(studentUser);
            System.out.println("Kaydedilen Öğrenci: " + studentUser.getUsername());


            Teacher teacherUser = new Teacher();
            teacherUser.setName("Ogretmen");
            teacherUser.setSurname("Soyadi");
            teacherUser.setAge(35);
            teacherUser.setEmail("teacher@example.com");
            teacherUser.setUsername("teacheruser");
            teacherUser.setPassword(passwordEncoder.encode("teacherpassword")); 
            teacherUser.setRole("ROLE_TEACHER");
            teacherUser.setEnabled(false); 
            teacherUser.setActive(true);
            teacherUser.setCreatedDate(LocalDateTime.now());
            teacherUser.setUpdatedDate(LocalDateTime.now());
            teacherUser.setSubject("Matematik");
            teacherUser.setGraduateSchool("Ankara Üniversitesi");
            teacherUser.setDiplomaNumber("T12345");
            userRepository.save(teacherUser);
            System.out.println("Kaydedilen Öğretmen: " + teacherUser.getUsername());


            System.out.println("Varsayılan kullanıcı ekleme tamamlandı.");


            System.out.println("\nBaşlangıç Veri Ekleme ve Temel Okuma Testleri:");

            System.out.println("\nTüm Kullanıcılar:");
            List<User> users = userRepository.findAll(); 
            users.forEach(user -> System.out.println(user.getUsername() + " - " + user.getRole() + " - Enabled: " + user.isEnabled()));

            System.out.println("\nKullanıcı adı 'studentuser' olan Kullanıcı:");
            Optional<User> userById = users.stream()
                                            .filter(u -> u.getUsername().equals("studentuser"))
                                            .findFirst();
            userById.ifPresent(user -> System.out.println(user.getUsername()));

            System.out.println("\nKullanıcı adı 'teacheruser' olan Kullanıcı:");
            Optional<User> userByUsernameOptional = userRepository.findByUsername("teacheruser");
            userByUsernameOptional.ifPresent(user -> System.out.println(user.getUsername()));

            System.out.println("\nRolü 'ROLE_STUDENT' olan Kullanıcılar:");
            List<User> students = userRepository.findAllByRole("ROLE_STUDENT");
            students.forEach(user -> System.out.println(user.getUsername()));

            System.out.println("\nBaşlangıç testleri tamamlandı.");
        };
    }
}