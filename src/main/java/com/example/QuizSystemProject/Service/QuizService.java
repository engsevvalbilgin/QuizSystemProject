package com.example.QuizSystemProject.Service; // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.*; // Model katmanındaki tüm Entity'leri import edin

import com.example.QuizSystemProject.Repository.*; // Repository katmanındaki tüm Repository'leri import edin
import java.util.ArrayList;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service; // Service anotasyonunu import edin
import com.example.QuizSystemProject.exception.UserNotFoundException; // UserNotFoundException importu
import java.lang.IllegalArgumentException; // IllegalArgumentException importu
import com.example.QuizSystemProject.exception.QuizNotFoundException; // <--- YENİ EKLENEN İMPORT
import com.example.QuizSystemProject.exception.UserNotAuthorizedException; // <--- YENİ EKLENEN İMPORT
import com.example.QuizSystemProject.exception.QuestionTypeNotFoundException; // <--- YENİ EKLENECEK
import com.example.QuizSystemProject.exception.QuestionDoesNotBelongToQuizException;
import com.example.QuizSystemProject.exception.QuestionNotFoundException;
import com.example.QuizSystemProject.exception.InvalidQuestionTypeForOptionException; // <--- YENİ EKLENECEK
import com.example.QuizSystemProject.exception.OptionNotFoundException;

import java.util.List; // List importu
import java.util.Optional; // Optional importu


@Service // Spring'e bu sınıfın bir Service bileşeni olduğunu belirtir
public class QuizService {

    // Bu servisin ihtiyaç duyacağı Repository'ler ve diğer Servisler
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository; // Quiz oluştururken Teacher'ı bulmak için
    private final QuestionTypeRepository questionTypeRepository; // Soru tipi seçerken veya bulurken

    // Bağımlılıkların enjekte edildiği constructor
    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository,
                       OptionRepository optionRepository, UserRepository userRepository,
                       QuestionTypeRepository questionTypeRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.userRepository = userRepository;
        this.questionTypeRepository = questionTypeRepository;
    }
 // Bir Quize ait tüm soruları getirme
 // Quiz Controller'da /api/quizzes/{quizId}/questions GET endpoint'i için kullanılacak
     @Transactional(readOnly = true) // Sadece okuma işlemi
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

             // 4. Quize ait soruları çek
             // Quiz Entity'sindeki getQuestions() metodu kullanılabilir.
             // JPA, OneToMany ilişkisini varsayılan olarak Lazy yükler, bu metod çağrıldığında sorular yüklenecektir.
             // Eğer Eager yükleme isteseydik, Quiz Entity'deki ilişki tanımını değiştirmemiz gerekirdi.
             List<Question> questions = quiz.getQuestions();


             System.out.println("QuizService: Quiz ID " + quizId + " icin " + questions.size() + " adet soru getirildi.");

             // 5. Sorular listesini döndür
             return questions;
         }
 // --- Sizin template'lerinizdeki ilgili işlevlere karşılık gelen metot imzaları ---

    // Quiz Oluşturma (Teacher yetkisi gerektirecek)
    // Sizin Quiz.java template'indeki createQuiz ve Teacher.java template'indeki createQuiz metotlarının mantığı burada birleşiyor.
    @Transactional // Sınıf seviyesinde Transactional var, burada tekrar gerekmez ama belirtmek zararlı değil
    public Quiz createQuiz(int teacherId, String name, String description, Integer durationMinutes) {
        System.out.println("QuizService: Quiz olusturma başlatıldı - Ogretmen ID: " + teacherId + ", Ad: " + name);

        // teacherId ile Teacher kullanıcısını bulma
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> {
                    System.err.println("QuizService: Ogretmen bulunamadi - ID: " + teacherId);
                    return new UserNotFoundException("ID " + teacherId + " olan öğretmen bulunamadı."); // UserNotFoundException fırlat
                });

        // Bu kullanıcının rolünün TEACHER olduğunu kontrol etme
        if (!"ROLE_TEACHER".equals(teacher.getRole())) {
            System.err.println("QuizService: Kullanici ogretmen yetkisine sahip degil - ID: " + teacherId + ", Rol: " + teacher.getRole());
            throw new IllegalArgumentException("ID " + teacherId + " olan kullanıcının quiz oluşturma yetkisi yok."); // Yetki hatası fırlat
        }
        System.out.println("QuizService: Ogretmen bulundu ve yetkisi dogrulandi - Kullanici Adi: " + teacher.getUsername());


        // 5. Yeni Quiz nesnesini oluştur
        Quiz newQuiz = new Quiz();
        newQuiz.setName(name);
        if (teacher instanceof Teacher) {
            newQuiz.setTeacher((Teacher) teacher);
        } else {
            // Handle case where user is not a Teacher, perhaps throw an exception or log error
            System.err.println("QuizService: User ID " + teacherId + " is not a Teacher. Quiz creation failed.");
            throw new UserNotAuthorizedException("User is not a Teacher, cannot create quiz.");
        }
        newQuiz.setDescription(description);
        if (durationMinutes != null) {
            newQuiz.setDuration(durationMinutes); // Lombok generates setDuration(int)
        }
        // isActive is true by default in Quiz constructor. startDate and endDate can be set if needed.
        // newQuiz.setStartDate(new Date()); // Example if you want to set start date to now, öğretmen sonradan aktif eder

        // Kaydetme ve döndürme
        Quiz createdQuiz = quizRepository.save(newQuiz);
        System.out.println("QuizService: Quiz başarıyla oluşturuldu - ID: " + createdQuiz.getId());

        return createdQuiz;
    }

 // Quiz Güncelleme (Teacher yetkisi gerektirecek)
 // Sizin Quiz.java template'indeki updateQuiz metodunun mantığı burada.
     @Transactional // Sınıf seviyesinde Transactional var, burada tekrar gerekmez ama belirtmek zararlı değil
 // Method imzasına güncelleyen kullanıcının ID'si eklendi
     public Quiz updateQuiz(int quizId, String name, String description, Integer durationMinutes, boolean isActive, int updaterUserId) {
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


     // 3. Yetki kontrolü: Güncelleyen kullanıcı ADMIN mi VEYA quizin öğretmeni mi?
     boolean isTeacher = quizToUpdate.getTeacher() != null && quizToUpdate.getTeacher().getId() == updaterUserId;
     boolean isAdmin = "ROLE_ADMIN".equals(updaterUser.getRole());

     if (!isAdmin && !isTeacher) {
     System.err.println("QuizService: Kullanicinin quizi güncelleme yetkisi yok - Kullanici ID: " + updaterUserId + ", Quiz ID: " + quizId);
     throw new UserNotAuthorizedException("Bu quizi güncellemek için yetkiniz yok."); // UserNotAuthorizedException fırlat
     }
     System.out.println("QuizService: Kullanici yetkisi dogrulandi.");


     // 4. Güncel bilgileri set et (DTO'dan gelen null olmayan alanları güncelle)
     // String alanlar için null ve boş/boşluklu string kontrolü yapalım
     if (name != null && !name.trim().isEmpty()) {
         quizToUpdate.setName(name.trim());
     }
     if (description != null && !description.trim().isEmpty()) {
         quizToUpdate.setDescription(description.trim());
     }
     // Integer alan null olabilir, sadece null değilse set edelim
     if (durationMinutes != null) {
         if (durationMinutes < 0) {
             throw new IllegalArgumentException("Süre negatif olamaz.");
         }
         quizToUpdate.setDuration(durationMinutes); // Uses setDuration(int)
     }
     quizToUpdate.setActive(isActive);

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
     @Transactional // Sınıf seviyesinde var
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
     @Transactional(readOnly = true) // Sadece okuma işlemi olduğu için readOnly = true yapmak performansı artırabilir
     public List<Quiz> getAllQuizzes() {
         System.out.println("QuizService: Tüm quizler getiriliyor.");
         // Repository'den tüm Quiz Entity'lerini çek
         List<Quiz> quizzes = quizRepository.findAll();

         System.out.println("QuizService: " + quizzes.size() + " adet quiz bulundu.");
         // Quiz listesini döndür
         return quizzes;
     }

  // ID'ye Göre Quiz Getirme
     @Transactional(readOnly = true) // Sadece okuma işlemi
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

  // Bir Öğretmenin Quizlerini Getirme
     @Transactional(readOnly = true) // Sadece okuma işlemi
     public List<Quiz> getQuizzesByTeacher(int teacherId) {
         System.out.println("QuizService: Öğretmen quizleri getiriliyor - Ogretmen ID: " + teacherId);

         // 1. teacherId ile Kullanıcıyı bul
         User teacher = userRepository.findById(teacherId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Ogretmen bulunamadi - ID: " + teacherId);
                 return new UserNotFoundException("ID " + teacherId + " olan öğretmen bulunamadı."); // UserNotFoundException fırlat
             });

         // 2. Kullanıcının rolünün TEACHER olduğunu kontrol et
         if (!"ROLE_TEACHER".equals(teacher.getRole())) {
             System.err.println("QuizService: Kullanici ogretmen yetkisine sahip degil - ID: " + teacherId + ", Rol: " + teacher.getRole());
             throw new IllegalArgumentException("ID " + teacherId + " olan kullanıcının öğretmen rolü yok."); // Yetki/Rol hatası fırlat
         }
         System.out.println("QuizService: Ogretmen bulundu ve yetkisi dogrulandi - Kullanici Adi: " + teacher.getUsername());


         // 3. Repository'deki özel metodu kullanarak öğretmene ait tüm quizleri çek
         List<Quiz> quizzes = quizRepository.findAllByTeacher(teacher);

         System.out.println("QuizService: Öğretmen ID " + teacherId + " icin " + quizzes.size() + " adet quiz bulundu.");

         // 4. Quiz listesini döndür
         return quizzes;
     }


  // --- Soru Yönetimi (Teacher yetkisi gerektirecek) ---
     // Sizin Question.java template'indeki createQuestion, deleteQuestion, updateQuestion
     // ve Quiz.java template'indeki addQuestion, removeQuestion, updateQuestion metotlarının mantığı burada birleşiyor.

     // Quize Yeni Soru Ekleme
     @Transactional // Sınıf seviyesinde var
     // Method imzasına ekleyen kullanıcının ID'si eklendi
     public Question addQuestionToQuiz(int quizId, int number, String questionSentence, String correctAnswerText, int questionTypeId, List<Option> options, int adderUserId) {
         System.out.println("QuizService: Quize soru ekleme başlatıldı - Quiz ID: " + quizId + ", Ekleyen Kullanıcı ID: " + adderUserId);

         // 1. Quizi bul
         Quiz quiz = quizRepository.findById(quizId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Soru eklenecek quiz bulunamadi - ID: " + quizId);
                 return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı."); // QuizNotFoundException fırlat
             });
         System.out.println("QuizService: Quiz bulundu - Ad: " + quiz.getName());

         // 2. Soru Tipini bul
         QuestionType questionType = questionTypeRepository.findById(questionTypeId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Soru tipi bulunamadi - ID: " + questionTypeId);
                 return new QuestionTypeNotFoundException("ID " + questionTypeId + " olan soru tipi bulunamadı."); // QuestionTypeNotFoundException fırlat
             });
         System.out.println("QuizService: Soru tipi bulundu - Ad: " + questionType.getTypeName());


         // 3. Ekleme işlemini yapan kullanıcıyı bul (Yetki kontrolü için)
         User adderUser = userRepository.findById(adderUserId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Soru ekleyen kullanıcı bulunamadi - ID: " + adderUserId);
                 return new UserNotFoundException("ID " + adderUserId + " olan kullanıcı bulunamadı."); // UserNotFoundException fırlat
             });
         System.out.println("QuizService: Soru ekleyen kullanıcı bulundu - Kullanici Adi: " + adderUser.getUsername());


         // 4. Yetki kontrolü: Ekleyen kullanıcı ADMIN mi VEYA quizin öğretmeni mi?
         boolean isTeacher = quiz.getTeacher() != null && quiz.getTeacher().getId() == adderUserId;
         boolean isAdmin = "ROLE_ADMIN".equals(adderUser.getRole());

         if (!isAdmin && !isTeacher) {
             System.err.println("QuizService: Kullanicinin quize soru ekleme yetkisi yok - Kullanici ID: " + adderUserId + ", Quiz ID: " + quizId);
             throw new UserNotAuthorizedException("Bu quize soru eklemek için yetkiniz yok."); // UserNotAuthorizedException fırlat
         }
         System.out.println("QuizService: Kullanici yetkisi dogrulandi.");


         // 5. Yeni Question objesi oluştur
         // Quiz ve QuestionType objelerini set ediyoruz
         // Not: Doğru cevap metni (correctAnswerText) sadece metin tabanlı sorular için geçerlidir.
         // Çoktan seçmeli sorular için doğru şıklar Option Entity'sinde belirtilir.
         // Soru tipine göre correctAnswerText veya options işlenmesi gerekebilir.
         // 5. Yeni Question objesi oluştur
         Question newQuestion = new Question();
         newQuestion.setNumber(number);
         newQuestion.setQuestionSentence(questionSentence);
         newQuestion.setType(questionType);
         // newQuestion.setQuiz(quiz); // Quiz will be set via quiz.addQuestion(newQuestion) later for bidirectionality

         // Handle correctAnswerText based on question type
         // Assuming "Çoktan Seçmeli" is the Turkish name for Multiple Choice
         if (!"Çoktan Seçmeli".equalsIgnoreCase(questionType.getTypeName()) && correctAnswerText != null && !correctAnswerText.isEmpty()) {
             QuestionAnswer correctAnswerEntity = new QuestionAnswer();
             correctAnswerEntity.setAnswer(correctAnswerText);
             correctAnswerEntity.setCorrect(true); // This IS the correct answer for the question
             correctAnswerEntity.setQuestion(newQuestion); // Link back to the question
             // Note: takeQuizId would be null here as this is the definitive answer, not a user's submission
             newQuestion.setAnswer(correctAnswerEntity);
         }


         // 6. Sağlanan şıkları (options) yeni soruya ekle
         if (options != null && !options.isEmpty()) {
             // Sadece çoktan seçmeli sorulara şık eklenebileceğini kontrol etmek iyi practice olur
             // if ("Çoktan Seçmeli".equals(questionType.getTypeName())) { ... }
             options.forEach(optionRequest -> {
                 // DTO'dan gelen Option objesini Question Entity'deki Option listesine eklemeden önce
                 // Question objesini Option'a set etmeliyiz (ilişkiyi kurmak için).
                 // Veya DTO'dan Option Entity'sine dönüşüm yapıp, Option Entity'sini eklemeliyiz.
                 // Varsayım: Gelen List<Option> aslında OptionCreateRequest'ten dönüştürülmüş Option Entity objeleridir.
                 // QuizService createQuiz metodunda List<Option> options parametresi vardı, burada da aynı varsayımı sürdürüyoruz.
                 // Eğer Controller'dan OptionCreateRequest listesi geliyorsa, burada o DTO'ları Option Entity'sine dönüştürmek gerekir.

                 // Gelen 'options' listesi DTO veya unmanaged entities olabilir.
                 // newQuestion.addOption() helper methodu Question-Option ilişkisini kurmalı.
                 // Option.setQuestion(newQuestion) helper methodu içinde çağrılır.
                 newQuestion.addOption(optionRequest); // Question entity'deki yardımcı metodu kullan
             });
         }
         // else {
         //    // Eğer soru çoktan seçmeli ise ve şıklar boş/null ise hata verilebilir
         // }


         // 7. Yeni soruyu Quize ekle
         quiz.addQuestion(newQuestion); // Quiz Entity'deki yardımcı metodu kullan


         // 8. Quizi kaydet (Question ve Optionlar cascade sayesinde otomatik kaydedilir/güncellenir)
         // Kaydetme sonucunda güncel Question objesini geri alabiliriz
         // Quiz kaydetmek yerine doğrudan questionRepository.save(newQuestion) da yapılabilir,
         // ancak ilişkiyi Quiz tarafından yönetiyorsak Quizi kaydetmek tutarlıdır.
         Quiz savedQuiz = quizRepository.save(quiz); // Quizi kaydet, cascades to newQuestion and its parts

         // After saving the quiz, the newQuestion entity (if it was part of the quiz's question list
         // and cascade is set up correctly) should have its ID generated.
         // However, to reliably get the ID of the *saved* question, it's often better to find it from the savedQuiz:
         Question savedQuestion = savedQuiz.getQuestions().stream()
            .filter(q -> q.getNumber() == number && q.getQuestionSentence().equals(questionSentence) && q.getId() == 0) // Heuristic to find unsaved
            .findFirst()
            .orElseGet(() -> savedQuiz.getQuestions().stream().filter(q -> q.getNumber() == number && q.getQuestionSentence().equals(questionSentence)).reduce((first, second) -> second).orElse(null)); // Fallback: find by number/sentence, take last if multiple
         
         if (savedQuestion != null && savedQuestion.getId() != 0) {
            System.out.println("QuizService: Soru başarıyla quize eklendi ve quiz kaydedildi - Quiz ID: " + savedQuiz.getId() + ", Yeni Soru ID: " + savedQuestion.getId());
            return savedQuestion; // Return the managed entity with ID
         } else {
            // Fallback or error handling if question isn't found in saved quiz's list as expected
            System.err.println("QuizService: Eklenen soru, kaydedilen quiz listesinde bulunamadı veya ID'si atanmadı. newQuestion ID: " + newQuestion.getId());
            return newQuestion; // Return the original, potentially unmanaged or ID-less entity
         }


         // 9. Eklenen Question objesini döndür
         // This line is now part of the new logic above to return the savedQuestion with ID.
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
     // Method imzasına güncelleyen kullanıcının ID'si eklendi
     public Question updateQuestionInQuiz(int quizId, int questionId, int number, String questionSentence, String correctAnswerText, int questionTypeId, List<Option> options, int updaterUserId) {
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

    // 5. Yetki kontrolü: Güncelleyen kullanıcı ADMIN mi VEYA quizin öğretmeni mi?
    boolean isTeacher = quiz.getTeacher() != null && quiz.getTeacher().getId() == updaterUserId;
    boolean isAdmin = "ROLE_ADMIN".equals(updaterUser.getRole());

    if (!isAdmin && !isTeacher) {
        System.err.println("QuizService: Kullanicinin quizdeki soruyu güncelleme yetkisi yok - Kullanici ID: " + updaterUserId + ", Quiz ID: " + quizId);
        throw new UserNotAuthorizedException("Bu quizdeki soruyu güncellemek için yetkiniz yok.");
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

    // 8. Correct Answer ve Options güncellemesi
    // Assuming "Çoktan Seçmeli" is the Turkish name for Multiple Choice
    if (!"Çoktan Seçmeli".equalsIgnoreCase(questionType != null ? questionType.getTypeName() : questionToUpdate.getType().getTypeName())) {
        // For non-multiple-choice questions, update or set the QuestionAnswer
        if (correctAnswerText != null && !correctAnswerText.isEmpty()) {
            QuestionAnswer currentAnswer = questionToUpdate.getAnswer();
            if (currentAnswer == null) {
                currentAnswer = new QuestionAnswer();
                currentAnswer.setQuestion(questionToUpdate); // Link to parent question
                // takeQuizId would be null as this is the definitive answer, not a submission
            }
            currentAnswer.setAnswer(correctAnswerText);
            currentAnswer.setCorrect(true); // This IS the correct answer for the question
            questionToUpdate.setAnswer(currentAnswer); // Set the (new or updated) answer to the question
            // For non-MCQ, ensure options list is cleared if it's not supposed to have them
            if (questionToUpdate.getOptions() != null && !questionToUpdate.getOptions().isEmpty()) {
                // Before clearing, ensure JPA manages removal correctly
                // One way is to iterate and call removeOption helper if it handles DB removal via orphanRemoval on Question.options
                new ArrayList<>(questionToUpdate.getOptions()).forEach(questionToUpdate::removeOption);
            }

        } else {
            // For multiple-choice questions, the single 'answer' field on Question should be null
            if (questionToUpdate.getAnswer() != null) {
                questionToUpdate.setAnswer(null);
            }
            // Update options for multiple-choice questions
            // A common strategy: clear existing options and add new ones.
            // This ensures the question's options list exactly matches the request.
            if (questionToUpdate.getOptions() != null) {
                 new ArrayList<>(questionToUpdate.getOptions()).forEach(questionToUpdate::removeOption);
            }
            if (options != null) {
                for (Option optionRequest : options) {
                    // Assuming optionRequest might be a new DTO or unmanaged entity.
                    // The addOption helper should handle setting the question back-reference.
                    questionToUpdate.addOption(optionRequest);
                }
            }
        } // Closing brace for the 'else' block (MCQ options handling)
    } // Closing brace for the main if(!"Çoktan Seçmeli"...) / else block

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


     // --- Diğer İşlevler ---
     // Sizin Quiz.java ve Teacher.java template'lerindeki istatistik, AI gibi metotlar
     // showQuizStatistics, showQuizAnswers, askAiToAnswerQuiz, CalculateAverage, AskAItoGradeQuiz
     // Bunlar StatisticsService veya AIService gibi ayrı servislere taşınacak.
}