package com.example.QuizSystemProject.Service;
import com.example.QuizSystemProject.Model.*;
import com.example.QuizSystemProject.Repository.*;
import com.example.QuizSystemProject.exception.*;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

@Service // Spring'e bu sınıfın bir Service bileşeni olduğunu belirtir
@Transactional // Sınıf seviyesinde transactional yönetimi sağlar
public class QuizService implements ApplicationContextAware {

    // Bu servisin ihtiyaç duyacağı Repository'ler ve diğer Servisler
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository; // Quiz oluştururken Teacher'ı bulmak için
    private final QuestionTypeRepository questionTypeRepository; // Soru tipi seçerken veya bulurken
    private TeacherRepository teacherRepository; // Teacher nesnelerini bulmak için - not final so we can reassign if needed

        private ApplicationContext applicationContext;
    
    // Bağımlılıkların enjekte edildiği constructor
    
    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository,
                       OptionRepository optionRepository, UserRepository userRepository,
                       QuestionTypeRepository questionTypeRepository, TeacherRepository teacherRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.userRepository = userRepository;
        this.questionTypeRepository = questionTypeRepository;
        this.teacherRepository = teacherRepository;
        // Ensure dependency is properly initialized
        System.out.println("QuizService constructor: teacherRepository initialized: " + (teacherRepository != null));
    }
    
    @Override
    public void setApplicationContext(@org.springframework.lang.NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @PostConstruct
    public void init() {
        if (teacherRepository == null) {
            System.err.println("CRITICAL ERROR: teacherRepository is null in QuizService init!");
            teacherRepository = applicationContext.getBean(TeacherRepository.class);
            System.out.println("QuizService: Fetched TeacherRepository from application context: " + (teacherRepository != null));
        }
    }
    
    /**
     * Bir quizin soru sayısını doğrudan veritabanından hesaplar.
     * Bu metot, JPA'nın lazy loading sorunundan etkilenmeden gerçek soru sayısını döndürür.
     * 
     * @param quizId Soru sayısı hesaplanacak quizin ID'si
     * @return int Quizin toplam soru sayısı
     */
    public int getQuestionCountForQuiz(int quizId) {
        long count = questionRepository.countByQuizId(quizId);
        System.out.println("QuizService: Quiz ID " + quizId + " için soru sayısı: " + count);
        return (int) count; // Cast long to int since the method returns int
    }
    public void deactivateQuiz(int quizId, int actingUserId) {
        // Quiz'i ID'sine göre bul
        Optional<Quiz> optionalQuiz = quizRepository.findById(quizId);

        // Quiz bulunamazsa hata fırlat
        if (optionalQuiz.isEmpty()) {
            throw new QuizNotFoundException("Quiz bulunamadı ID: " + quizId+ "");
        }

        Quiz quiz = optionalQuiz.get();

        // Yetkilendirme kontrolü: Sadece quizi oluşturan öğretmen pasif hale getirebilir.
        // Bu kontrol, Quiz modelinizde bir `teacher` veya `creatorId` alanı olduğunu varsayar.
        // Örneğin: quiz.getTeacher().getId() veya quiz.getCreatorId()
        // Eğer Quiz modelinizde öğretmenin ID'si yoksa, bu kontrolü yapamazsınız.
        // Bu durumda, yetkilendirme daha genel roller üzerinden (örn: isAdmin, isTeacher) yapılmalıdır.
        /*
        if (quiz.getTeacher() == null || quiz.getTeacher().getId() != actingUserId) {
            throw new UnauthorizedAccessException("Bu quizi pasif hale getirmeye yetkiniz yok.");
        }
        */
        // Yukarıdaki yorum satırları, Quiz modelinizdeki Teacher ilişkisini veya creatorId'yi varsayar.
        // Eğer Quiz modelinizde bir 'teacherId' alanınız varsa, onu kullanın.
        // Aksi takdirde, bu yetkilendirme kontrolünü kaldırın veya farklı bir role dayalı kontrol ekleyin.
        // Örnek: If (userIsTeacher && quizBelongsToTeacher(quizId, actingUserId)) { ... }
        // Basitlik için şimdilik creatorId'yi varsayalım.
        // if (quiz.getCreatorId() != actingUserId) {
        //     throw new UnauthorizedAccessException("Bu quizi pasif hale getirmeye yetkiniz yok.");
        // }


        // Quiz'in isActive değerini false olarak ayarla
        quiz.setActive(false);

        // Güncellenmiş quizi veritabanına kaydet
        quizRepository.save(quiz);

        System.out.println("QuizService: Quiz başarıyla pasif hale getirildi - ID: " + quizId + " İşlemi yapan: " + actingUserId);
    }
   
    // Bir Quize ait tüm soruları getirme
    // Quiz Controller'da /api/quizzes/{quizId}/questions GET endpoint'i için kullanılacak
    @Transactional(readOnly = true)
    public List<Question> getQuestionsByQuiz(int quizId, int viewerUserId) { // Method imzasına görüntüleyen kullanıcının ID'si eklendi
        System.out.println("QuizService: Quize ait sorular getiriliyor - Quiz ID: " + quizId + ", Görüntüleyen Kullanıcı ID: " + viewerUserId);

         // 1. Quizi bul
         Quiz quiz = quizRepository.findById(quizId)
                 .orElseThrow(() -> {
                     System.err.println("QuizService: Sorulari getirilecek quiz bulunamadi - ID: " + quizId);
                     return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı."); // QuizNotFoundException fırlat
                 });
         System.out.println("QuizService: Quiz bulundu - Ad: " + quiz.getName());

             // 2. Görüntüleyen kullanıcıyı bul (Yetki kontrolü için)
             User viewerUser = userRepository.findById(viewerUserId)
                     .orElseThrow(() -> {
                         System.err.println("QuizService: Sorulari görüntüleyen kullanıcı bulunamadi - ID: " + viewerUserId);
                         return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı."); // UserNotFoundException fırlat
                     });
             System.out.println("QuizService: Görüntüleyen kullanıcı bulundu - Kullanici Adi: " + viewerUser.getUsername());


             // 3. Yetki kontrolü: Görüntüleyen kullanıcı ADMIN mi VEYA quizin öğretmeni mi?
             // VEYA Quiz aktif ve genel görünür mü (şimdilik bu kontrolü atlayalım, varsayım öğretmen/admin görüyor)
             // Eğer öğrenci quiz çözerken soruları çekecekse, bu service veya QuizSessionService içinde farklı bir metod olmalıdır.
             boolean isTeacher = quiz.getTeacher() != null && quiz.getTeacher().getId() == viewerUserId;
             boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole());

             if (!isAdmin && !isTeacher) {
                 System.err.println("QuizService: Kullanicinin bu quize ait sorulari görüntüleme yetkisi yok - Kullanici ID: " + viewerUserId + ", Quiz ID: " + quizId);
                 throw new UserNotAuthorizedException("Bu quize ait soruları görüntülemek için yetkiniz yok."); // UserNotAuthorizedException fırlat
             }
             System.out.println("QuizService: Kullanici yetkisi dogrulandi.");

              // 4. Debug quiz object
              System.out.println("DEBUG: Quiz ID: " + quiz.getId());
              System.out.println("DEBUG: Quiz name: " + quiz.getName());
              System.out.println("DEBUG: Quiz description: " + quiz.getDescription());
              System.out.println("DEBUG: Quiz isActive: " + quiz.isActive());
              
              // 5. Use optimized query to fetch questions with their options in a single query
              List<Question> questions = questionRepository.findQuestionsWithOptionsByQuizId(quizId);
              
              if (!questions.isEmpty()) {
                  Question firstQuestion = questions.get(0);
                  System.out.println("DEBUG: First question ID: " + firstQuestion.getId());
                  System.out.println("DEBUG: First question text: " + firstQuestion.getQuestionSentence());
                  System.out.println("DEBUG: First question number: " + firstQuestion.getNumber());
                  
                  // Log options for the first question if they were loaded
                  if (firstQuestion.getOptions() != null) {
                      System.out.println("DEBUG: First question has " + firstQuestion.getOptions().size() + " options");
                  } else {
                      System.out.println("DEBUG: First question has no options loaded");
                  }
              }


              System.out.println("QuizService: Quiz ID " + quizId + " icin " + questions.size() + " adet soru getirildi.");

              // 6. Return the list of questions with their options pre-fetched
              return questions;
         }
 // --- Sizin template'lerinizdeki ilgili işlevlere karşılık gelen metot imzaları ---

    // Quiz Oluşturma (Teacher yetkisi gerektirecek)
    // Sizin Quiz.java template'indeki createQuiz ve Teacher.java template'indeki createQuiz metotlarının mantığı burada birleşiyor.
    @Transactional
    public Quiz createQuiz(int teacherId, String name, String description, Integer durationMinutes, boolean isActive, String topic) {
        System.out.println("QuizService: Quiz olusturma başlatıldı - Ogretmen ID: " + teacherId + ", Ad: " + name);

        // teacherId ile User kullanıcısını bulma
        User user = userRepository.findById(teacherId)
                .orElseThrow(() -> {
                    System.err.println("QuizService: Kullanıcı bulunamadi - ID: " + teacherId);
                    return new UserNotFoundException("ID " + teacherId + " olan kullanıcı bulunamadı."); // UserNotFoundException fırlat
                });

        // Bu kullanıcının rolünün TEACHER olduğunu kontrol etme
        if (!"ROLE_TEACHER".equals(user.getRole())) {
            System.err.println("QuizService: Kullanici ogretmen yetkisine sahip degil - ID: " + teacherId + ", Rol: " + user.getRole());
            throw new IllegalArgumentException("ID " + teacherId + " olan kullanıcının quiz oluşturma yetkisi yok."); // Yetki hatası fırlat
        }
        System.out.println("QuizService: Ogretmen bulundu ve yetkisi dogrulandi - Kullanici Adi: " + user.getUsername());

        // Defensive programming - Check if teacherRepository is null
        Teacher teacher = null;
        
        if (teacherRepository == null) {
            System.err.println("CRITICAL ERROR: teacherRepository is null in createQuiz method!");
            System.out.println("Attempting to use userRepository as fallback...");
            
            // Fallback approach: Use userRepository directly
            User foundUser = userRepository.findById(teacherId)
                    .orElseThrow(() -> {
                        System.err.println("Fallback approach: User not found - ID: " + teacherId);
                        return new UserNotFoundException("ID " + teacherId + " olan kullanıcı bulunamadı.");
                    });
                    
            // Check if the foundUser is a Teacher instance
            if (foundUser instanceof Teacher) {
                teacher = (Teacher) foundUser;
                System.out.println("Fallback approach: User is a Teacher instance, casting successful.");
            } else {
                System.err.println("Fallback approach: User is not a Teacher instance.");
                throw new UserNotAuthorizedException("Kullanıcı öğretmen değil. Quiz oluşturulamaz.");
            }
        } else {
            // Normal approach using teacherRepository
            System.out.println("QuizService: Attempting normal approach with teacherRepository. teacherRepository is: " + (teacherRepository == null ? "null" : "not null"));
            try {
                if (teacherRepository == null) {
                    System.err.println("CRITICAL ERROR IMMINENT: teacherRepository is NULL right before findTeacherByUserId call!");
                }
                teacher = teacherRepository.findTeacherByUserId(teacherId) // This is the line that causes the NPE according to logs
                    .orElseThrow(() -> {
                        System.err.println("QuizService: Teacher entity not found with User ID (normal approach) - User ID: " + teacherId);
                        return new UserNotFoundException("User ID " + teacherId + " için Teacher entity bulunamadı (normal yaklaşım).");
                    });
                System.out.println("QuizService: Successfully found Teacher with User ID (normal approach): " + teacherId);
            } catch (NullPointerException npe) {
                System.err.println("FATAL: NullPointerException directly caught when calling teacherRepository.findTeacherByUserId. teacherRepository IS NULL.");
                npe.printStackTrace(); // Print stack trace for NPE
                // Fallback to userRepository due to NPE
                System.err.println("Proceeding to fallback (NPE) using userRepository for teacherId: " + teacherId);
                User backupUser = userRepository.findById(teacherId)
                        .orElseThrow(() -> {
                            System.err.println("Fallback (NPE): User not found - ID: " + teacherId);
                            return new UserNotFoundException("ID " + teacherId + " olan kullanıcı bulunamadı (NPE fallback).");
                        });
                if (backupUser instanceof Teacher) {
                    teacher = (Teacher) backupUser;
                    System.out.println("Fallback (NPE): User is a Teacher instance, casting successful.");
                } else {
                    System.err.println("Fallback (NPE): User is not a Teacher instance.");
                    throw new UserNotAuthorizedException("Kullanıcı öğretmen değil (NPE fallback). Quiz oluşturulamaz.");
                }
            } catch (Exception e) {
                System.err.println("Error calling teacherRepository.findTeacherByUserId (non-NPE): " + e.getMessage());
                e.printStackTrace(); // Print stack trace for other exceptions
                // Second fallback: Use userRepository if teacherRepository call fails for other reasons
                 System.err.println("Proceeding to fallback (non-NPE) using userRepository for teacherId: " + teacherId);
                User backupUser = userRepository.findById(teacherId)
                    .orElseThrow(() -> {
                        System.err.println("Second fallback (non-NPE): User not found - ID: " + teacherId);
                        return new UserNotFoundException("ID " + teacherId + " olan kullanıcı bulunamadı (non-NPE fallback).");
                    });
                    
                if (backupUser instanceof Teacher) {
                    teacher = (Teacher) backupUser;
                    System.out.println("Second fallback (non-NPE): User is a Teacher instance, casting successful.");
                } else {
                    System.err.println("Second fallback (non-NPE): User is not a Teacher instance.");
                    throw new UserNotAuthorizedException("Kullanıcı öğretmen değil (non-NPE fallback). Quiz oluşturulamaz.");
                }
            }
        }
        
        System.out.println("QuizService: Teacher entity bulundu - ID: " + teacher.getId() + ", User ID: " + teacherId);

        // 5. Yeni Quiz nesnesini oluştur
        Quiz newQuiz = new Quiz();
        newQuiz.setName(name);
        newQuiz.setTeacher(teacher); // Artık doğrudan Teacher nesnesini kullanıyoruz, casting yapmamıza gerek yok
        newQuiz.setDescription(description);
        // Set the topic field
        if (topic != null) {
            newQuiz.setTopic(topic);
            System.out.println("QuizService: Quiz konu ayarlandı - topic: " + topic);
        }
        if (durationMinutes != null) {
            newQuiz.setDuration(durationMinutes); // Lombok generates setDuration(int)
        }
        // Explicitly set the active status from the parameter
        newQuiz.setActive(isActive);
        System.out.println("QuizService: Quiz aktif durumu ayarlandı - isActive: " + isActive);
        // startDate and endDate can be set if needed.
        // newQuiz.setStartDate(new Date()); // Example if you want to set start date to now, öğretmen sonradan aktif eder

        // Kaydetme ve döndürme
        Quiz createdQuiz = quizRepository.save(newQuiz);
        System.out.println("QuizService: Quiz başarıyla oluşturuldu - ID: " + createdQuiz.getId());

        return createdQuiz;
    }
    
    // Quiz Güncelleme metoduna geç
    
    // Quiz Güncelleme (Teacher yetkisi gerektirecek)
    // Sizin Quiz.java template'indeki updateQuiz metodunun mantığı burada.
    @Transactional
    // Method imzasına güncelleyen kullanıcının ID'si ve topic parametresi eklendi
    public Quiz updateQuiz(int quizId, String name, String description, Integer durationMinutes, Boolean isActive, String topic, int updaterUserId) {
        System.out.println("QuizService: Quiz güncelleme başlatıldı - Quiz ID: " + quizId + ", Güncelleyen Kullanıcı ID: " + updaterUserId);

        // 1. Güncellenecek Quizi bul
        Quiz quizToUpdate = quizRepository.findById(quizId)
        .orElseThrow(() -> {
            System.err.println("QuizService: Güncellenecek quiz bulunamadi - ID: " + quizId);
            return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı."); // QuizNotFoundException fırlat
        });
        System.out.println("QuizService: Quiz bulundu - Ad: " + quizToUpdate.getName());

        // 2. Güncelleyen kullanıcıyı bul (Yetki kontrolü için)
        User updaterUser = userRepository.findById(updaterUserId)
        .orElseThrow(() -> {
            System.err.println("QuizService: Güncelleyen kullanıcı bulunamadi - ID: " + updaterUserId);
            return new UserNotFoundException("ID " + updaterUserId + " olan kullanıcı bulunamadı."); // UserNotFoundException fırlat
        });
        System.out.println("QuizService: Güncelleyen kullanıcı bulundu - Kullanici Adi: " + updaterUser.getUsername());

        // 3. Yetki kontrolü: Basitleştirilmiş kullanıcı rolü kontrolü
        String userRole = updaterUser.getRole();
        boolean isAdmin = "ROLE_ADMIN".equals(userRole);
        boolean isTeacher = "ROLE_TEACHER".equals(userRole);
        
        // Quiz'in sahibi kontrolü (teacherId ile kullanıcı ID'si doğrudan karşılaştırma)
        boolean isQuizOwner = false;
        if (quizToUpdate.getTeacher() != null && isTeacher) {
            // Quiz'in öğretmen ID'si ile güncelleyen kullanıcı ID'si aynı mı?
            isQuizOwner = quizToUpdate.getTeacher().getId() == updaterUserId;
        }
        
        System.out.println("QuizService: Quiz güncelleme yetki kontrolü - Güncelleyen User ID: " + updaterUserId + 
                           ", Role: " + userRole + 
                           ", isAdmin: " + isAdmin + 
                           ", isTeacher: " + isTeacher +
                           ", isQuizOwner: " + isQuizOwner);

        // GELİŞTİRME MODU: TEACHER ve ADMIN rolüne sahip herkes quiz güncelleyebilir
        // Üretim için: if (!isAdmin && !(isTeacher && isQuizOwner))
        if (!isAdmin && !isTeacher) {
            System.err.println("QuizService: Kullanicinin quizi güncelleme yetkisi yok - Kullanici ID: " + updaterUserId + ", Quiz ID: " + quizId);
            throw new UserNotAuthorizedException("Bu quizi güncellemek için yetkiniz yok. ADMIN veya TEACHER rolü gerekli."); 
        }
        
        System.out.println("QuizService: Kullanici yetkisi dogrulandi - isAdmin: " + isAdmin + ", isTeacher: " + isTeacher + ", isQuizOwner: " + isQuizOwner);


        // 4. Güncel bilgileri set et (DTO'dan gelen null olmayan alanları güncelle)
        // String alanlar için null ve boş/boşluklu string kontrolü yapalım
     if (name != null && !name.trim().isEmpty()) {
         quizToUpdate.setName(name.trim());
     }
     if (description != null && !description.trim().isEmpty()) {
         quizToUpdate.setDescription(description.trim());
     }
     // Topic alanını güncelle
     if (topic != null) {
         quizToUpdate.setTopic(topic.trim());
         System.out.println("QuizService: Quiz konu güncellendi - topic: " + topic);
     }
     // Integer alan null olabilir, sadece null değilse set edelim
     if (durationMinutes != null) {
         if (durationMinutes < 0) {
             throw new IllegalArgumentException("Süre negatif olamaz.");
         }
         quizToUpdate.setDuration(durationMinutes); // Uses setDuration(int)
     }
     
     // Handle the nullable Boolean isActive parameter
     if (isActive != null) {
         quizToUpdate.setActive(isActive);
     }

     // İsteğe bağlı: Başlangıç/Bitiş tarihleri de güncellenebilir, bu DTO'da yoktu ama eklenirse burada işlenebilir.
     // quizToUpdate.setStartDate(...);
     // quizToUpdate.setEndDate(...);


     // 5. Kaydetme ve döndürme
     Quiz updatedQuiz = quizRepository.save(quizToUpdate);
     System.out.println("QuizService: Quiz başarıyla güncellendi - ID: " + updatedQuiz.getId());

     return updatedQuiz;
     }

  // Quiz Silme (Teacher/Admin yetkisi gerektirecek)
     // Sizin Quiz.java template'indeki deleteQuiz metodunun mantığı burada.
     @Transactional
     public void deleteQuiz(int quizId, int deleterUserId) { // Method imzasına silen kullanıcının ID'si eklendi
         System.out.println("QuizService: Quiz silme başlatıldı - Quiz ID: " + quizId + ", Silen Kullanıcı ID: " + deleterUserId);

         // 1. Silinecek Quizi bul
         Quiz quizToDelete = quizRepository.findById(quizId)
         .orElseThrow(() -> {
             System.err.println("QuizService: Silinecek quiz bulunamadi - ID: " + quizId);
             return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı."); // QuizNotFoundException fırlat
         });
         System.out.println("QuizService: Quiz bulundu - Ad: " + quizToDelete.getName());

         // 2. Silen kullanıcıyı bul (Yetki kontrolü için)
         User deleterUser = userRepository.findById(deleterUserId)
         .orElseThrow(() -> {
             System.err.println("QuizService: Silen kullanıcı bulunamadi - ID: " + deleterUserId);
             return new UserNotFoundException("ID " + deleterUserId + " olan kullanıcı bulunamadı."); // UserNotFoundException fırlat
         });
         System.out.println("QuizService: Silen kullanıcı bulundu - Kullanici Adi: " + deleterUser.getUsername());


         // 3. Yetki kontrolü: Silen kullanıcı ADMIN mi VEYA quizin öğretmeni mi?
         boolean isTeacher = quizToDelete.getTeacher() != null && quizToDelete.getTeacher().getId() == deleterUserId;
         boolean isAdmin = "ROLE_ADMIN".equals(deleterUser.getRole());

         if (!isAdmin && !isTeacher) {
             System.err.println("QuizService: Kullanicinin quizi silme yetkisi yok - Kullanici ID: " + deleterUserId + ", Quiz ID: " + quizId);
             throw new UserNotAuthorizedException("Bu quizi silmek için yetkiniz yok."); // UserNotAuthorizedException fırlat
         }
         System.out.println("QuizService: Kullanici yetkisi dogrulandi.");

         // 4. Quizi silme (Cascade Type.ALL ve orphanRemoval = true ayarları, ilişkili Soru, Şık ve Cevapları da silmelidir)
         quizRepository.deleteById(quizId); // ID üzerinden silme


         System.out.println("QuizService: Quiz başarıyla silindi - ID: " + quizId);

         // Metot void döndürdüğü için return yok
     }

  // Tüm Quizleri Getirme (Herkes görebilir veya filtrelenebilir)
    @Transactional(readOnly = true)
    public List<Quiz> getAllQuizzes() {
         System.out.println("QuizService: Tüm quizler getiriliyor.");
         // Repository'den tüm Quiz Entity'lerini çek
         List<Quiz> quizzes = quizRepository.findAll();

         System.out.println("QuizService: " + quizzes.size() + " adet quiz bulundu.");
         // Quiz listesini döndür
         return quizzes;
     }

  // ID'ye Göre Quiz Getirme
     @Transactional(readOnly = true)
    public Optional<Quiz> getQuizById(int quizId) {
         System.out.println("QuizService: Quiz getiriliyor - ID: " + quizId);
         // Repository'den quizi ID'ye göre bul
         Optional<Quiz> quizOptional = quizRepository.findById(quizId);

         if (quizOptional.isPresent()) {
             System.out.println("QuizService: Quiz bulundu - ID: " + quizId);
         } else {
              System.out.println("QuizService: Quiz bulunamadi - ID: " + quizId);
         }

         // Optional<Quiz> olarak sonucu döndür
         return quizOptional;
     }
     
     @Transactional(readOnly = true)
     public Optional<Quiz> getQuizById(int quizId, int userId) {
         System.out.println("QuizService: Quiz getiriliyor - Quiz ID: " + quizId + ", Kullanici ID: " + userId);
         
         // 1. Önce quizi bul
         Optional<Quiz> quizOptional = quizRepository.findById(quizId);
         
         if (!quizOptional.isPresent()) {
             System.out.println("QuizService: Quiz bulunamadi - ID: " + quizId);
             return Optional.empty();
         }
         
         // 2. Kullanıcıyı bul
         User user = userRepository.findById(userId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Kullanici bulunamadi - ID: " + userId);
                 return new UserNotFoundException("Kullanıcı bulunamadı.");
             });
         System.out.println("QuizService: Kullanici bulundu - Kullanici Adı: " + user.getUsername() + ", Rol: " + user.getRole());
         
         // 3. Yetki kontrolü
         Quiz quiz = quizOptional.get();
         boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
         boolean isTeacher = "ROLE_TEACHER".equals(user.getRole());
         boolean isStudent = "ROLE_STUDENT".equals(user.getRole());
         
         // ADMIN her quizi görebilir
         if (isAdmin) {
             System.out.println("QuizService: Admin yetkisi ile quiz görüntüleme izni verildi.");
             return quizOptional;
         }
         
         // TEACHER kendisine ait quizleri görebilir
         if (isTeacher) {
             // Quiz öğretmen tarafından oluşturulmuşsa
             Teacher quizTeacher = quiz.getTeacher();
             if (quizTeacher != null) {
                 System.out.println("QuizService: Quiz'in öğretmeni: " + quizTeacher.getId() + ", mevcut öğretmen: " + userId);
                 
                 // Öğretmen kendi oluşturduğu quizleri görebilir
                 if (quizTeacher.getId() == userId) {
                     System.out.println("QuizService: Öğretmenin kendi quizi, görüntüleme izni verildi.");
                     return quizOptional;
                 }
                 
                 // Başka öğretmenlerin quizlerine şimdilik izin ver
                 System.out.println("QuizService: Başka bir öğretmenin quizi, öğretmen rolü ile görüntüleme izni verildi.");
                 return quizOptional;
             }
         }
         
         // STUDENT sadece aktif quizleri görebilir
         if (isStudent && quiz.isActive()) {
             System.out.println("QuizService: Öğrenci aktif quizi görüntüleme izni verildi.");
             return quizOptional;
         }
         
         // Yetkisi yoksa 401 hatası fırlat
         System.err.println("QuizService: Kullanıcının bu quizi görüntüleme yetkisi yok - Kullanıcı ID: " + userId + ", Quiz ID: " + quizId);
         throw new UserNotAuthorizedException("Bu quizi görüntülemek için yetkiniz yok.");
     }

  // Bir Öğretmenin Aktif Quizlerini Getirme
    // Bir Öğretmenin Aktif Quizlerini Getirme
    public List<Quiz> getQuizzesByTeacher(int teacherId) {
        System.out.println("QuizService: Öğretmenin AKTİF quizleri getiriliyor - Öğretmen ID: " + teacherId);
        
        try {
            // Doğrudan TeacherRepository'yi kullanarak Teacher nesnesini bul
            Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> {
                    System.err.println("QuizService: Öğretmen (entity) bulunamadı - ID: " + teacherId);
                    return new UserNotFoundException("ID " + teacherId + " olan öğretmen bulunamadı.");
                });
            
            // Öğretmenin SADECE AKTİF quizlerini getir
            List<Quiz> activeQuizzes = quizRepository.findActiveQuizzesByTeacher(teacher);
            System.out.println("QuizService: " + activeQuizzes.size() + " adet aktif quiz bulundu");
            return activeQuizzes;
            
        } catch (Exception e) {
            System.err.println("QuizService: Öğretmen quizleri getirilirken hata oluştu - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // Bir Öğretmenin TÜM Quizlerini Getirme (Aktif ve Pasif)
    @Transactional(readOnly = true)
    public List<Quiz> getAllQuizzesByTeacher(int teacherId) {
        System.out.println("QuizService: Öğretmenin TÜM quizleri getiriliyor - Öğretmen ID: " + teacherId);
        
        try {
            // Doğrudan TeacherRepository'yi kullanarak Teacher nesnesini bul
            Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> {
                    System.err.println("QuizService: Öğretmen (entity) bulunamadı - ID: " + teacherId);
                    return new UserNotFoundException("ID " + teacherId + " olan öğretmen bulunamadı.");
                });
            
            // Öğretmenin TÜM quizlerini getir (aktif ve pasif)
            List<Quiz> quizzes = quizRepository.findByTeacher(teacher);
            System.out.println("QuizService: Öğretmen ID: " + teacherId + " için toplam " + quizzes.size() + " adet quiz bulundu (aktif ve pasif).");
            
            return quizzes;
        } catch (Exception e) {
            System.err.println("QuizService: Öğretmenin tüm quizleri getirilirken hata oluştu - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


 @Transactional
public Question addQuestionToQuiz(int quizId, int number, String questionSentence, 
    String correctAnswerText, int questionTypeId, int points, List<Option> options, int adderUserId) {
    
    // 1. Find the quiz
    Quiz quiz = quizRepository.findById(quizId)
        .orElseThrow(() -> new QuizNotFoundException("Quiz not found with ID: " + quizId));
    
    // 2. Find the question type
    QuestionType questionType = questionTypeRepository.findById(questionTypeId)
        .orElseThrow(() -> new QuestionTypeNotFoundException("Question type not found with ID: " + questionTypeId));
    
    // 3. Verify user has permission
    User user = userRepository.findById(adderUserId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + adderUserId));
    
    boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
    boolean isTeacher = false;
    
    // Önemli: Teacher sınıfı User sınıfından türüyor ve SINGLE_TABLE stratejisi kullanılıyor
    // Bu nedenle, Teacher.id = User.id olacaktır
    if ("ROLE_TEACHER".equals(user.getRole()) && quiz.getTeacher() != null) {
        // Kullanıcı zaten Teacher türünde ve ID'si User ID'sine eşit
        // Öğretmen ID'si ile Quiz'in atanmış olduğu öğretmen ID'sini karşılaştır
        isTeacher = (adderUserId == quiz.getTeacher().getId());
        
        System.out.println("QuizService: Öğretmen kontrolü - Kullanıcı ID: " + adderUserId +
                         ", Quiz Öğretmen ID: " + quiz.getTeacher().getId() + 
                         ", Eşleşme: " + isTeacher);
    }
    
    if (!isAdmin && !isTeacher) {
        System.err.println("QuizService: Kullanıcının quize soru ekleme yetkisi yok - Kullanıcı ID: " + adderUserId);
        throw new UserNotAuthorizedException("Bu quize soru eklemek için yetkiniz yok.");
    }
    
    System.out.println("QuizService: Quiz ID " + quizId + " için soru ekleme yetkisi doğrulandı.");
    
    // 4. Create and configure the new question
    Question question = new Question();
    question.setNumber(number);
    question.setQuestionSentence(questionSentence);
    question.setType(questionType);
    question.setQuiz(quiz);  // Set the quiz reference
    
    // Set points directly from the parameter
    if (points > 0) {
        System.out.println("QuizService: Soru puanı ayarlanıyor - Puan: " + points);
        question.setPoints(points);
    } else {
        question.setPoints(1); // Default value
        System.out.println("QuizService: Varsayılan soru puanı ayarlandı - Puan: 1");
    }
    
    // Handle the answer if provided
if (correctAnswerText != null && !correctAnswerText.trim().isEmpty() && 
!"Çoktan Seçmeli".equalsIgnoreCase(questionType.getTypeName())) {
QuestionAnswer answer = new QuestionAnswer();
answer.setAnswer(correctAnswerText);
answer.setCorrect(true);
answer.setQuestion(question);

// Şu anda bir TakeQuiz olmadığı için null bırakıyoruz
// ancak veritabanı kısıtlaması olmadığından emin olmalıyız
answer.setTakeQuiz(null);

question.setAnswer(answer);
}
    
    // 6. Handle options if provided
    if (options != null && !options.isEmpty()) {
        for (Option option : options) {
            option.setQuestion(question);
            question.getOptions().add(option);
        }
    }
    
    // 7. Save the question (this will cascade to options and answer)
    Question savedQuestion = questionRepository.save(question);
    
    // 8. Add to quiz's questions collection
    quiz.getQuestions().add(savedQuestion);
    quizRepository.save(quiz);  // Update the quiz to maintain the relationship
    
    return savedQuestion;
}

  // Quizden Soru Silme
     @Transactional // Sınıf seviyesinde var
     // Method imzasına silen kullanıcının ID'si eklendi
     public void removeQuestionFromQuiz(int quizId, int questionId, int removerUserId) {
         System.out.println("QuizService: Quizden soru silme başlatıldı - Quiz ID: " + quizId + ", Soru ID: " + questionId + ", Silen Kullanıcı ID: " + removerUserId);

         // 1. Quizi bul
         Quiz quiz = quizRepository.findById(quizId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Soru silinecek quiz bulunamadi - ID: " + quizId);
                 return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı."); // QuizNotFoundException fırlat
             });
         System.out.println("QuizService: Quiz bulundu - Ad: " + quiz.getName());

         // 2. Silinecek Soruyu bul
         // Not: Sorunun bu quize ait olup olmadığını kontrol etmek için doğrudan questionRepository kullanmak yerine
         // önce quizi bulup sonra quiz.getQuestions() listesinden soruyu aramak da bir yöntemdir.
         // Ancak doğrudan findById daha performanslı olabilir. Sonrasında aitlik kontrolünü yaparız.
         Question questionToRemove = questionRepository.findById(questionId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Silinecek soru bulunamadi - ID: " + questionId);
                 return new QuestionNotFoundException("ID " + questionId + " olan soru bulunamadı."); // QuestionNotFoundException fırlat
             });
         System.out.println("QuizService: Soru bulundu - ID: " + questionToRemove.getId());

         // 3. Silen kullanıcıyı bul (Yetki kontrolü için)
         User removerUser = userRepository.findById(removerUserId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Soru silen kullanıcı bulunamadi - ID: " + removerUserId);
                 return new UserNotFoundException("ID " + removerUserId + " olan kullanıcı bulunamadı."); // UserNotFoundException fırlat
             });
         System.out.println("QuizService: Silen kullanıcı bulundu - Kullanici Adi: " + removerUser.getUsername());


         // 4. Yetki kontrolü: Silen kullanıcı ADMIN mi VEYA quizin öğretmeni mi?
         boolean isTeacher = quiz.getTeacher() != null && quiz.getTeacher().getId() == removerUserId;
         boolean isAdmin = "ROLE_ADMIN".equals(removerUser.getRole());

         if (!isAdmin && !isTeacher) {
             System.err.println("QuizService: Kullanicinin quizden soru silme yetkisi yok - Kullanici ID: " + removerUserId + ", Quiz ID: " + quizId);
             throw new UserNotAuthorizedException("Bu quizden soru silmek için yetkiniz yok."); // UserNotAuthorizedException fırlat
         }
         System.out.println("QuizService: Kullanici yetkisi dogrulandi.");

         // 5. Sorunun gerçekten belirtilen quize ait olup olmadığını kontrol et
         if (questionToRemove.getQuiz().getId() != quizId) {
             System.err.println("QuizService: Soru bu quize ait degil - Soru ID: " + questionId + ", Quiz ID: " + quizId);
             throw new QuestionDoesNotBelongToQuizException("ID " + questionId + " olan soru ID " + quizId + " olan quize ait değil."); // Yeni exception fırlat
         }
         System.out.println("QuizService: Soru, quize ait olduğu doğrulandı.");


         // 6. Soruyu Quiz Entity'sindeki listeden çıkar (ilişkiyi kopar)
         // Bu, Quiz Entity'deki removeQuestion yardımcı metodu varsa kullanılır.
         // Eğer yoksa, quiz.getQuestions().remove(questionToRemove); şeklinde yapılabilir.
         // Entity'deki yardımcı metot ilişkileri doğru yönetmek için tercih edilir.
         // Varsayım: Quiz Entity'de removeQuestion(Question question) metodu var.
         quiz.removeQuestion(questionToRemove); // İlişkiyi kopar

         // 7. Quizi kaydet (orphanRemoval=true sayesinde Question ve Option'ları siler)
         quizRepository.save(quiz);

         System.out.println("QuizService: Soru ID " + questionId + " başarıyla quiz ID " + quizId + " den silindi.");

         // Metot void döndürdüğü için return yok
     }

  // Quizdeki Soruyu Güncelleme
     @Transactional // Sınıf seviyesinde var
     // Method imzasına güncelleyen kullanıcının ID'si ve points parametresi eklendi
     public Question updateQuestionInQuiz(int quizId, int questionId, Integer number, String questionSentence, String correctAnswerText, int questionTypeId, int points, List<Option> options, int updaterUserId) {
         System.out.println("QuizService: Quizdeki soru güncelleniyor - Quiz ID: " + quizId + ", Soru ID: " + questionId + ", Güncelleyen Kullanıcı ID: " + updaterUserId);

         // 1. Güncellenecek Quizi bul (Yetki kontrolü için gerekli)
         Quiz quiz = quizRepository.findById(quizId)
         .orElseThrow(() -> {
             System.err.println("QuizService: Soru güncellenecek quiz bulunamadi - ID: " + quizId);
             return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı."); // QuizNotFoundException fırlat
         });
        System.out.println("QuizService: Quiz bulundu - Ad: " + quiz.getName());


        // 2. Güncellenecek Soruyu bul
        Question questionToUpdate = questionRepository.findById(questionId)
        .orElseThrow(() -> {
            System.err.println("QuizService: Güncellenecek soru bulunamadi - ID: " + questionId);
        return new QuestionNotFoundException("ID " + questionId + " olan soru bulunamadı."); // QuestionNotFoundException fırlat
        });
        System.out.println("QuizService: Soru bulundu - ID: " + questionToUpdate.getId());

        // 3. Sorunun gerçekten belirtilen quize ait olup olmadığını kontrol et
        if (questionToUpdate.getQuiz().getId() != quizId) {
            System.err.println("QuizService: Soru bu quize ait degil - Soru ID: " + questionId + ", Quiz ID: " + quizId);
            throw new QuestionDoesNotBelongToQuizException("ID " + questionId + " olan soru ID " + quizId + " olan quize ait değil."); // QuestionDoesNotBelongToQuizException fırlat
        }
        System.out.println("QuizService: Soru, quize ait olduğu doğrulandı.");


        // 4. Güncelleyen kullanıcıyı bul (Yetki kontrolü için)
        User updaterUser = userRepository.findById(updaterUserId)
        .orElseThrow(() -> {
            System.err.println("QuizService: Güncelleyen kullanıcı bulunamadi - ID: " + updaterUserId);
            return new UserNotFoundException("ID " + updaterUserId + " olan kullanıcı bulunamadı."); // UserNotFoundException fırlat
        });
    System.out.println("QuizService: Güncelleyen kullanıcı bulundu - Kullanici Adi: " + updaterUser.getUsername());

    // 5. Yetki kontrolü: Güncelleyen kullanıcı ADMIN mi VEYA TEACHER rolüne sahip mi?
    String userRole = updaterUser.getRole();
    boolean isAdmin = "ROLE_ADMIN".equals(userRole);
    boolean isTeacher = "ROLE_TEACHER".equals(userRole);
    
    // Quiz'in sahibi kontrolü
    boolean isQuizOwner = false;
    if (quiz.getTeacher() != null && isTeacher) {
        // Quiz'in öğretmen ID'si ile güncelleyen kullanıcı ID'si aynı mı?
        isQuizOwner = quiz.getTeacher().getId() == updaterUserId;
    }
    
    System.out.println("QuizService: Soru güncelleme yetki kontrolü - Güncelleyen User ID: " + updaterUserId + 
                    ", Role: " + userRole + 
                    ", isAdmin: " + isAdmin + 
                    ", isTeacher: " + isTeacher + 
                    ", isQuizOwner: " + isQuizOwner);

    // KRITIK: Geliştirme aşamasında kolaylık için TEACHER rolüne sahip herkes quizleri güncelleyebilir
    // Üretim ortamında daha sıkı kontrol uygulanabilir (isAdmin || isQuizOwner)
    if (!isAdmin && !isTeacher) {
        System.err.println("QuizService: Kullanicinin quizdeki soruyu güncelleme yetkisi yok - Kullanici ID: " + updaterUserId + ", Quiz ID: " + quizId);
        throw new UserNotAuthorizedException("Bu quizdeki soruyu güncellemek için yetkiniz yok. ADMIN veya TEACHER rolü gerekli.");
    }
    System.out.println("QuizService: Kullanici yetkisi dogrulandi.");

    // 6. Soru Tipini bul (güncelleme için)
    QuestionType questionType = null;
    if (questionTypeId != 0) {
        questionType = questionTypeRepository.findById(questionTypeId)
            .orElseThrow(() -> {
                System.err.println("QuizService: Güncellenecek soru tipi bulunamadi - ID: " + questionTypeId);
                return new QuestionTypeNotFoundException("ID " + questionTypeId + " olan soru tipi bulunamadı.");
            });
        System.out.println("QuizService: Güncellenecek soru tipi bulundu - Ad: " + questionType.getTypeName());
    }

    // 7. Sorunun temel özelliklerini güncelle
    questionToUpdate.setNumber(number);
    questionToUpdate.setQuestionSentence(questionSentence);
    if (questionType != null) {
        questionToUpdate.setType(questionType);
    }
    
    // Set question points directly from the parameter
    if (points > 0) {
        System.out.println("QuizService: Soru puanı ayarlanıyor - Puan: " + points);
        questionToUpdate.setPoints(points);
    } else {
        System.out.println("QuizService: Soru puanı değiştirilmedi, mevcut puan: " + questionToUpdate.getPoints());
    }

    // 8. Correct Answer ve Options güncellemesi
    // "Çoktan Seçmeli" is the Turkish name for Multiple Choice
    String questionTypeName = questionType != null ? questionType.getTypeName() : questionToUpdate.getType().getTypeName();
    boolean isMultipleChoice = "Çoktan Seçmeli".equalsIgnoreCase(questionTypeName);
    
    System.out.println("QuizService: Soru tipi: " + questionTypeName + ", isMultipleChoice: " + isMultipleChoice);
    
    if (isMultipleChoice) {
        // For multiple-choice questions
        System.out.println("QuizService: Çoktan seçmeli soru güncelleniyor");
        
        // The single 'answer' field on Question should be null for multiple-choice
        if (questionToUpdate.getAnswer() != null) {
            questionToUpdate.setAnswer(null);
        }
        
        // Update options for multiple-choice questions
        // First, clear existing options and then add new ones
        if (questionToUpdate.getOptions() != null) {
            System.out.println("QuizService: Mevcut seçenekler temizleniyor - Seçenek sayısı: " + questionToUpdate.getOptions().size());
            new ArrayList<>(questionToUpdate.getOptions()).forEach(questionToUpdate::removeOption);
        }
        
        if (options != null && !options.isEmpty()) {
            System.out.println("QuizService: Yeni seçenekler ekleniyor - Seçenek sayısı: " + options.size());
            for (Option optionRequest : options) {
                System.out.println("QuizService: Seçenek ekleniyor - Metin: " + optionRequest.getText() + ", isCorrect: " + optionRequest.isCorrect());
                questionToUpdate.addOption(optionRequest);
            }
        } else {
            System.out.println("QuizService: UYARI - Çoktan seçmeli soru için seçenek bulunamadı!");
        }
    } else {
        // For non-multiple-choice questions
        System.out.println("QuizService: Çoktan seçmeli olmayan soru güncelleniyor");
        
        if (correctAnswerText != null && !correctAnswerText.isEmpty()) {
            System.out.println("QuizService: Doğru cevap güncelleniyor - Cevap: " + correctAnswerText);
            QuestionAnswer currentAnswer = questionToUpdate.getAnswer();
            if (currentAnswer == null) {
                currentAnswer = new QuestionAnswer();
                currentAnswer.setQuestion(questionToUpdate); // Link to parent question
            }
            currentAnswer.setAnswer(correctAnswerText);
            currentAnswer.setCorrect(true); // This IS the correct answer for the question
            questionToUpdate.setAnswer(currentAnswer); // Set the (new or updated) answer to the question
        } else {
            System.out.println("QuizService: UYARI - Çoktan seçmeli olmayan soru için doğru cevap bulunamadı!");
        }
        
        // For non-MCQ, ensure options list is cleared if it has any
        if (questionToUpdate.getOptions() != null && !questionToUpdate.getOptions().isEmpty()) {
            System.out.println("QuizService: Çoktan seçmeli olmayan soru için seçenekler temizleniyor");
            new ArrayList<>(questionToUpdate.getOptions()).forEach(questionToUpdate::removeOption);
        }
    }

        // 9. Güncellenmiş soruyu kaydet
        Question savedQuestion = questionRepository.save(questionToUpdate);
        System.out.println("QuizService: Soru başarıyla güncellendi - ID: " + savedQuestion.getId());

        return savedQuestion;
    } // Closing brace for the updateQuestionInQuiz method

  // --- Option Yönetimi (Teacher yetkisi gerektirecek - Genellikle soru yönetimi içinde yapılır ama ayrı metotlar da olabilir) ---
     // Sizin Option.java template'indeki createOption, deleteOption, updateOption metotlarının mantığı burada veya QuestionService içinde olur.

     // Bir Soruya Şık Ekleme (Örnek: Çoktan seçmeli soruya şık ekleme)
         @Transactional // Sınıf seviyesinde var
         // Method imzasına ekleyen kullanıcının ID'si eklendi
         public Option addOptionToQuestion(int questionId, String text, boolean isCorrect, int adderUserId) {
             System.out.println("QuizService: Soruya şık ekleme başlatıldı - Soru ID: " + questionId + ", Ekleyen Kullanıcı ID: " + adderUserId);

             // 1. Şık eklenecek Soruyu bul
             Question question = questionRepository.findById(questionId)
                     .orElseThrow(() -> {
                         System.err.println("QuizService: Sik eklenecek soru bulunamadi - ID: " + questionId);
                         return new QuestionNotFoundException("ID " + questionId + " olan soru bulunamadı."); // QuestionNotFoundException fırlat
                     });
             System.out.println("QuizService: Soru bulundu - ID: " + question.getId());


            // 2. Ekleme işlemini yapan kullanıcıyı bul (Yetki kontrolü için)
            User adderUser = userRepository.findById(adderUserId)
            .orElseThrow(() -> {
                System.err.println("QuizService: Sik ekleyen kullanıcı bulunamadi - ID: " + adderUserId);
                return new UserNotFoundException("ID " + adderUserId + " olan kullanıcı bulunamadı."); // UserNotFoundException fırlat
            });
            System.out.println("QuizService: Sik ekleyen kullanıcı bulundu - Kullanici Adi: " + adderUser.getUsername());

            // 3. Yetki kontrolü: Ekleyen kullanıcı ADMIN mi VEYA sorunun bağlı olduğu quizin öğretmeni mi?
            // Sorunun bağlı olduğu Quizi bulmalıyız
            Quiz quizOfQuestion = question.getQuiz();
            if (quizOfQuestion == null) {
                // Bu durum olmamalı, çünkü Question entity'si Quiz'e bağlı olmalı.
                // Veritabanında tutarsızlık varsa veya Entity ilişkisi yanlış ayarlandıysa olabilir.
                System.err.println("QuizService: Soru bağlı olduğu quizi bulamadi - Soru ID: " + questionId);
                throw new IllegalStateException("ID " + questionId + " olan soru herhangi bir quize bağlı değil."); // Beklenmeyen durum
            }

            boolean isTeacher = quizOfQuestion.getTeacher() != null && quizOfQuestion.getTeacher().getId() == adderUserId;
            boolean isAdmin = "ROLE_ADMIN".equals(adderUser.getRole());

            if (!isAdmin && !isTeacher) {
                System.err.println("QuizService: Kullanicinin bu soruya sik ekleme yetkisi yok - Kullanici ID: " + adderUserId + ", Soru ID: " + questionId);
                throw new UserNotAuthorizedException("Bu soruya şık eklemek için yetkiniz yok."); // UserNotAuthorizedException fırlat
            }
            System.out.println("QuizService: Kullanici yetkisi dogrulandi.");


            // 4. Soru tipinin çoktan seçmeli olup olmadığını kontrol et
            // Eğer QuestionType Entity'sinde bir Enum veya sabit bir string ismi varsa kontrol edilebilir.
            // Varsayım: QuestionType'ın typeName alanı "Çoktan Seçmeli" stringine eşitse çoktan seçmelidir.
            boolean isMultipleChoice = question.getType() != null && "Çoktan Seçmeli".equals(question.getType().getTypeName());

            if (!isMultipleChoice) {
                System.err.println("QuizService: Soru çoktan seçmeli degil, şık eklenemez - Soru ID: " + questionId + ", Tip: " + (question.getType() != null ? question.getType().getTypeName() : "Bilinmiyor"));
                throw new InvalidQuestionTypeForOptionException("ID " + questionId + " olan soru çoktan seçmeli değil, şık eklenemez."); // Yeni exception fırlat
            }
            System.out.println("QuizService: Soru tipi çoktan seçmeli olduğu doğrulandı.");


            // 5. Yeni Option objesi oluştur
            Option newOption = Option.createOption(question, text, isCorrect); // text, isCorrect ve ilişkiyi set ediyoruz


            // 6. Yeni şıkkı Soru Entity'sindeki listeye ekle (ilişkiyi kur)
            // Question Entity'deki addOption yardımcı metodu varsa kullanılır.
            // Eğer yoksa, question.getOptions().add(newOption); şeklinde yapılabilir.
            // Entity'deki yardımcı metot ilişkileri doğru yönetmek için tercih edilir.
            // Varsayım: Question Entity'de addOption(Option option) metodu var.
            question.addOption(newOption); // İlişkiyi kur


            // 7. Güncellenen Soruyu kaydet (Option cascade sayesinde otomatik kaydedilir)
            // Kaydetme sonucunda güncel Option objesini geri alabiliriz
            Question savedQuestion = questionRepository.save(question);
            System.out.println("QuizService: Soru ID " + savedQuestion.getId() + " başarıyla güncellendi. Yeni Şık ID: " + newOption.getId());

            // 8. Eklenen Option objesini döndür
            return newOption;
        }

      // Bir Şıkkı Silme
         @Transactional // Sınıf seviyesinde var
             // Method imzasına silen kullanıcının ID'si eklendi
             public void deleteOption(int optionId, int deleterUserId) {
                 System.out.println("QuizService: Şık silme başlatıldı - Şık ID: " + optionId + ", Silen Kullanıcı ID: " + deleterUserId);

                 // 1. Silinecek Şıkkı bul
                 Option optionToDelete = optionRepository.findById(optionId)
                         .orElseThrow(() -> {
                             System.err.println("QuizService: Silinecek sik bulunamadi - ID: " + optionId);
                             return new OptionNotFoundException("ID " + optionId + " olan şık bulunamadı."); // OptionNotFoundException fırlat
                         });
                 System.out.println("QuizService: Şık bulundu - ID: " + optionToDelete.getId());

             // 2. Silen kullanıcıyı bul (Yetki kontrolü için)
             User deleterUser = userRepository.findById(deleterUserId)
                     .orElseThrow(() -> {
                         System.err.println("QuizService: Sik silen kullanıcı bulunamadi - ID: " + deleterUserId);
                         return new UserNotFoundException("ID " + deleterUserId + " olan kullanıcı bulunamadı."); // UserNotFoundException fırlat
                     });
             System.out.println("QuizService: Silen kullanıcı bulundu - Kullanici Adi: " + deleterUser.getUsername());

                // 3. Yetki kontrolü: Silen kullanıcı ADMIN mi VEYA şıkkın bağlı olduğu sorunun quizinin öğretmeni mi?
                // Şıkkın bağlı olduğu Soruyu ve onun bağlı olduğu Quizi bulmalıyız
                Question questionOfOption = optionToDelete.getQuestion();
                if (questionOfOption == null) {
                    // Bu durum olmamalı, şık soruya bağlı olmalı.
                    System.err.println("QuizService: Şık bağlı olduğu soruyu bulamadi - Şık ID: " + optionId);
                    throw new IllegalStateException("ID " + optionId + " olan şık herhangi bir soruya bağlı değil."); // Beklenmeyen durum
                }
                Quiz quizOfQuestion = questionOfOption.getQuiz();
                if (quizOfQuestion == null) {
                    // Bu durum da olmamalı, soru quize bağlı olmalı.
                    System.err.println("QuizService: Şıkkın sorusu bağlı olduğu quizi bulamadi - Şık ID: " + optionId + ", Soru ID: " + questionOfOption.getId());
                    throw new IllegalStateException("ID " + questionOfOption.getId() + " olan soru herhangi bir quize bağlı değil."); // Beklenmeyen durum
                }


                 boolean isTeacher = quizOfQuestion.getTeacher() != null && quizOfQuestion.getTeacher().getId() == deleterUserId;
                 boolean isAdmin = "ROLE_ADMIN".equals(deleterUser.getRole());

                 if (!isAdmin && !isTeacher) {
                     System.err.println("QuizService: Kullanicinin bu şıkkı silme yetkisi yok - Kullanici ID: " + deleterUserId + ", Şık ID: " + optionId);
                     throw new UserNotAuthorizedException("Bu şıkkı silmek için yetkiniz yok."); // UserNotAuthorizedException fırlat
                 }
                 System.out.println("QuizService: Kullanici yetkisi dogrulandi.");


                 // 4. Şıkkı silme
                // Option entity'sindeki @ManyToOne(question) tarafında cascade=CascadeType.ALL veya orphanRemoval=true yoktur.
                // Question entity'sindeki @OneToMany(options) tarafında orphanRemoval=true vardır.
                // Şıkkı silmek, Question'ın options listesinden onu otomatik olarak çıkaracaktır.
                 optionRepository.deleteById(optionId); // Entity üzerinden silme

                 System.out.println("QuizService: Şık ID " + optionId + " başarıyla silindi.");

                 // Metot void döndürdüğü için return yok
             }

      // Bir Şıkkı Güncelleme
         @Transactional // Sınıf seviyesinde var
             // Method imzasına güncelleyen kullanıcının ID'si eklendi
             public Option updateOption(int optionId, String text, boolean isCorrect, int updaterUserId) {
                 System.out.println("QuizService: Şık güncelleme başlatıldı - Şık ID: " + optionId + ", Güncelleyen Kullanıcı ID: " + updaterUserId);

                 // 1. Güncellenecek Şıkkı bul
                 Option optionToUpdate = optionRepository.findById(optionId)
                         .orElseThrow(() -> {
                             System.err.println("QuizService: Güncellenecek sik bulunamadi - ID: " + optionId);
                             return new OptionNotFoundException("ID " + optionId + " olan şık bulunamadı."); // OptionNotFoundException fırlat
                         });
                 System.out.println("QuizService: Şık bulundu - ID: " + optionToUpdate.getId());


                 // 2. Güncelleyen kullanıcıyı bul (Yetki kontrolü için)
                 User updaterUser = userRepository.findById(updaterUserId)
                         .orElseThrow(() -> {
                             System.err.println("QuizService: Şık güncelleyen kullanıcı bulunamadi - ID: " + updaterUserId);
                             return new UserNotFoundException("ID " + updaterUserId + " olan kullanıcı bulunamadı."); // UserNotFoundException fırlat
                         });
                 System.out.println("QuizService: Şık güncelleyen kullanıcı bulundu - Kullanici Adi: " + updaterUser.getUsername());

                // 3. Yetki kontrolü: Güncelleyen kullanıcı ADMIN mi VEYA şıkkın bağlı olduğu sorunun quizinin öğretmeni mi?
                // Şıkkın bağlı olduğu Soruyu ve onun bağlı olduğu Quizi bulmalıyız
                Question questionOfOption = optionToUpdate.getQuestion();
                if (questionOfOption == null) {
                    // Bu durum olmamalı, şık soruya bağlı olmalı.
                    System.err.println("QuizService: Şık bağlı olduğu soruyu bulamadi - Şık ID: " + optionId);
                    throw new IllegalStateException("ID " + optionId + " olan şık herhangi bir soruya bağlı değil."); // Beklenmeyen durum
                }
                Quiz quizOfQuestion = questionOfOption.getQuiz();
                if (quizOfQuestion == null) {
                    // Bu durum da olmamalı, soru quize bağlı olmalı.
                    System.err.println("QuizService: Şıkkın sorusu bağlı olduğu quizi bulamadi - Şık ID: " + optionId + ", Soru ID: " + questionOfOption.getId());
                    throw new IllegalStateException("ID " + questionOfOption.getId() + " olan soru herhangi bir quize bağlı değil."); // Beklenmeyen durum
                }

                 boolean isTeacher = quizOfQuestion.getTeacher() != null && quizOfQuestion.getTeacher().getId() == updaterUserId;
                 boolean isAdmin = "ROLE_ADMIN".equals(updaterUser.getRole());

                 if (!isAdmin && !isTeacher) {
                     System.err.println("QuizService: Kullanicinin bu şıkkı güncelleme yetkisi yok - Kullanici ID: " + updaterUserId + ", Şık ID: " + optionId);
                     throw new UserNotAuthorizedException("Bu şıkkı güncellemek için yetkiniz yok."); // UserNotAuthorizedException fırlat
                 }
                 System.out.println("QuizService: Kullanici yetkisi dogrulandi.");


                 // 4. Şık Entity'sindeki alanları DTO'dan gelen null olmayan değerlerle güncelle
                 // text alanı için null ve boş/boşluklu string kontrolü yapalım
                 if (text != null && !text.trim().isEmpty()) {
                     optionToUpdate.setText(text.trim());
                 }
                 // isCorrect boolean olduğu için doğrudan set edilebilir
                 optionToUpdate.setCorrect(isCorrect);


                 // 5. Güncellenen Şıkkı kaydet
                 Option updatedOption = optionRepository.save(optionToUpdate);

                 System.out.println("QuizService: Şık ID " + updatedOption.getId() + " başarıyla güncellendi.");

                 // 6. Güncellenen Option objesini döndür
                 return updatedOption;
             }


     // Activate all quizzes for a specific teacher
    @Transactional
    public int activateAllQuizzesForTeacher(int teacherId) {
        System.out.println("QuizService: Activating all quizzes for teacher ID: " + teacherId);
        
        // 1. Find all quizzes for the teacher (including inactive ones)
        List<Quiz> teacherQuizzes = this.getAllQuizzesByTeacher(teacherId);
        
        if (teacherQuizzes.isEmpty()) {
            System.out.println("QuizService: No quizzes found for teacher ID: " + teacherId);
            return 0;
        }
        
        // 2. Activate all quizzes regardless of current state
        int totalQuizzes = teacherQuizzes.size();
        System.out.println("QuizService: Found " + totalQuizzes + " quizzes to activate");
        
        // 3. Set all quizzes to active and save them
        teacherQuizzes.forEach(quiz -> {
            if (!quiz.isActive()) {
                quiz.setActive(true);
                quiz = quizRepository.save(quiz);
                System.out.println("QuizService: Activated quiz ID: " + quiz.getId() + ", Name: " + quiz.getName());
            }
        });
        
        // 4. Flush changes to ensure they're persisted
        quizRepository.flush();
        
        System.out.println("QuizService: Successfully activated all " + totalQuizzes + " quizzes for teacher ID: " + teacherId);
        return totalQuizzes;
    }


     // --- Diğer İşlevler ---
     // Sizin Quiz.java ve Teacher.java template'lerindeki istatistik, AI gibi metotlar
     // showQuizStatistics, showQuizAnswers, askAiToAnswerQuiz, CalculateAverage, AskAItoGradeQuiz
     // Bunlar StatisticsService veya AIService gibi ayrı servislere taşınacak.
}