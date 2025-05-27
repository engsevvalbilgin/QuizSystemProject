package com.example.QuizSystemProject.Service;

import com.example.QuizSystemProject.Model.*;
import com.example.QuizSystemProject.Repository.*;
import com.example.QuizSystemProject.exception.*;
import com.example.QuizSystemProject.dto.StudentAnswerDto;
import com.example.QuizSystemProject.dto.QuizResultDto;
import com.example.QuizSystemProject.dto.AnswerType;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;



@Service // Spring'e bu sınıfın bir Service bileşeni olduğunu belirtir
// Bu servisteki metotların çoğu veritabanı işlemi içereceği için @Transactional kullanmak iyi practice'dir.
// Bir metot @Transactional olduğunda, metot içindeki tüm veritabanı işlemleri tek bir işlem (transaction) içinde gerçekleşir.
// Eğer bir hata olursa, işlem geri alınır (rollback).
@Transactional // Bu annotation'ı sınıf seviyesine koyarak tüm public metotlara uygulayabiliriz.
public class QuizSessionService {

    // Bu servisin ihtiyaç duyacağı Repository'ler
    private final QuizSessionRepository quizSessionRepository;
    private final AnswerAttemptRepository answerAttemptRepository; // Cevapları doğrudan kaydetmek için (opsiyonel, cascade ile de olur)
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository; // Öğrenciyi bulmak için

    // Bağımlılıkların enjekte edildiği constructor
    public QuizSessionService(QuizSessionRepository quizSessionRepository, AnswerAttemptRepository answerAttemptRepository,
                              QuizRepository quizRepository, QuestionRepository questionRepository,
                              OptionRepository optionRepository, UserRepository userRepository) {
        this.quizSessionRepository = quizSessionRepository;
        this.answerAttemptRepository = answerAttemptRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Quiz oturumunu tamamlar ve sonuçları hesaplar
     * @param sessionId Quiz oturum ID'si
     * @param studentId Öğrenci ID'si
     * @return Quiz sonuçlarını içeren DTO
     */
    @Transactional
    public QuizResultDto completeQuizSession(int sessionId, int studentId) {
        System.out.println("QuizSessionService: Quiz oturumu tamamlama istegi - Oturum ID: " + sessionId + ", Ogrenci ID: " + studentId);
        
        // Quiz oturumunu bul
        QuizSession session = quizSessionRepository.findById(sessionId)
            .orElseThrow(() -> {
                System.err.println("QuizSessionService: Oturum bulunamadi - ID: " + sessionId);
                return new RuntimeException("Quiz oturumu bulunamadı: " + sessionId);
            });
            
        // Yetki kontrolü
        if (session.getStudent().getId() != studentId) {
            System.err.println("QuizSessionService: Yetkisiz erisim denemesi - Oturum ID: " + sessionId + ", Istek Yapan ID: " + studentId);
            throw new RuntimeException("Bu sınava erişim yetkiniz yok");
        }
        
        // Oturum zaten tamamlanmışsa hata ver
        if (session.isCompleted()) {
            System.err.println("QuizSessionService: Oturum zaten tamamlanmis - Oturum ID: " + sessionId);
            throw new RuntimeException("Bu sınav zaten tamamlanmış");
        }
        
        // Oturumu tamamlandı olarak işaretle
        session.setCompleted(true);
        session.setEndTime(LocalDateTime.now());
        
        // Toplam süreyi hesapla (saniye cinsinden)
        long timeSpentSeconds = Duration.between(session.getStartTime(), session.getEndTime()).getSeconds();
        session.setTimeSpentSeconds((int) timeSpentSeconds);
        
        // Tüm cevapları al
        List<AnswerAttempt> answers = answerAttemptRepository.findByQuizSessionId(sessionId);
        System.out.println("QuizSessionService: " + answers.size() + " adet cevap bulundu");
        
        // Doğru cevap sayısını ve toplam puanı hesapla
        int correctCount = 0;
        int totalScore = 0;
        int maxPossibleScore = 0;
        
        // Her soru için doğruluk kontrolü yap
        for (AnswerAttempt answer : answers) {
            Question question = answer.getQuestion();
            maxPossibleScore += question.getPoints();
            
            // Log question type for debugging
            System.out.println("QuizSessionService: İşleniyor - Soru ID: " + question.getId() + 
                ", Tip: " + question.getType().getTypeName() + ", Max Puan: " + question.getPoints());
            
            // Açık uçlu sorular için AI puanını toplam puana ekle
            if ("OPEN_ENDED".equals(question.getType().getTypeName())) {
                // Use earnedPoints for open-ended questions
                int earnedPoints = answer.getEarnedPoints();
                System.out.println("QuizSessionService: Açık uçlu soru - Kazanılan Puan: " + earnedPoints + 
                    ", AI Puanı: " + answer.getAiScore());
                
                // If aiScore is set, use it; otherwise fall back to earnedPoints
                if (answer.getAiScore() > 0) {
                    totalScore += answer.getAiScore();
                    // Mark as correct if they earned any points
                    if (answer.getAiScore() > 0) {
                        correctCount++;
                    }
                    System.out.println("QuizSessionService: AI puanı kullanılıyor: " + answer.getAiScore());
                } else {
                    totalScore += earnedPoints;
                    // Mark as correct if they earned any points
                    if (earnedPoints > 0) {
                        correctCount++;
                    }
                    System.out.println("QuizSessionService: Kazanılan puan kullanılıyor: " + earnedPoints);
                }
            } else { // Çoktan seçmeli sorular için mevcut doğru/yanlış kontrolünü kullan
                if (answer.isCorrect()) {
                    correctCount++;
                    totalScore += question.getPoints();
                    System.out.println("QuizSessionService: Çoktan seçmeli soru doğru - Puan: " + question.getPoints());
                } else {
                    System.out.println("QuizSessionService: Çoktan seçmeli soru yanlış - Puan: 0");
                }
            }
        }
        
        // Skor bilgilerini güncelle
        session.setCorrectAnswers(correctCount);
        int scorePercentage = maxPossibleScore > 0 ? (int) ((totalScore * 100.0) / maxPossibleScore) : 0;
        session.setScore(scorePercentage); // Yüzde olarak kaydet
        session.setEarnedPoints(totalScore);
        
        // Değişiklikleri kaydet
        quizSessionRepository.save(session);
        
        // Sonuç DTO'sunu oluştur ve döndür
        QuizResultDto result = new QuizResultDto();
        result.setQuizId(session.getQuiz().getId());
        result.setQuizName(session.getQuiz().getName());
        result.setTotalQuestions(session.getQuiz().getQuestions().size());
        result.setCorrectAnswers(correctCount);
        result.setScore(scorePercentage);
        result.setTimeSpent((int) timeSpentSeconds);
        result.setPassed(scorePercentage >= session.getQuiz().getPassingScore());
        result.setPassingScore(session.getQuiz().getPassingScore());
        result.setTotalPoints(maxPossibleScore);
        result.setEarnedPoints(totalScore);
        
        // Soru sonuçlarını doldur
        List<QuizResultDto.QuestionResultDto> questionResults = new ArrayList<>();
        for (AnswerAttempt answer : answers) {
            QuizResultDto.QuestionResultDto qr = new QuizResultDto.QuestionResultDto();
            qr.setQuestionId(answer.getQuestion().getId());
            qr.setQuestionText(answer.getQuestion().getQuestionSentence());
            qr.setQuestionTypeId(answer.getQuestion().getType().getTypeName()); // Use getTypeName() for question type
            
            if (answer.getQuestion().getType().getTypeName().equals("MULTIPLE_CHOICE")) {
                // Çoktan seçmeli soru
                qr.setSelectedAnswerId(answer.getSelectedOptions() != null && !answer.getSelectedOptions().isEmpty() ? answer.getSelectedOptions().iterator().next().getId() : null);
                qr.setCorrect(answer.isCorrect());
                qr.setEarnedPoints(answer.getEarnedPoints());
            } else if (answer.getQuestion().getType().getTypeName().equals("OPEN_ENDED")) {
                // Açık uçlu soru
                qr.setTextAnswer(answer.getTextAnswer());
                qr.setRequiresManualGrading(answer.getAiScore() == 0); // Eğer AI puanı 0 ise manuel değerlendirme gerekebilir
                qr.setEarnedPoints(answer.getAiScore()); // AI tarafından verilen puanı kullan
                qr.setAiExplanation(answer.getAiExplanation());
                qr.setAiScore(answer.getAiScore());
            }
            
            qr.setPoints(answer.getQuestion().getPoints());
            questionResults.add(qr);
        }
        
        result.setQuestionResults(questionResults);
        
        System.out.println("QuizSessionService: Quiz oturumu basariyla tamamlandi - Toplam Puan: " + totalScore + "/" + maxPossibleScore);
        return result;
    }
    
    // --- Quiz Çözme Akışı Metotları ---

    // Quiz Oturumu Başlatma (Student yetkisi gerektirecek)
    // Sizin TakeQuiz template'ine karşılık gelen oturum başlatma kavramı burada.
    @Transactional // Oturum oluşturma ve kaydetme bir transaction içinde olmalı
    public QuizSession startQuizSession(int studentId, int quizId) {
        System.out.println("QuizSessionService: Quiz oturumu baslatma istegi - Ogrenci ID: " + studentId + ", Quiz ID: " + quizId);

        // studentId ile Öğrenci kullanıcısını bulma (Bulunamazsa UserNotFoundException fırlat)
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Oturum baslatma - Ogrenci bulunamadi - ID: " + studentId);
                    return new UserNotFoundException("ID " + studentId + " olan öğrenci bulunamadı.");
                });

        // quizId ile Quizi bulma (Bulunamazsa QuizNotFoundException fırlat)
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Oturum baslatma - Quiz bulunamadi - ID: " + quizId);
                    return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı.");
                });

        // Bu kullanıcının rolünün STUDENT olduğunu kontrol etme (Öğrenci değilse UserNotAuthorizedException fırlat)
        if (!"ROLE_STUDENT".equals(student.getRole())) {
             System.err.println("QuizSessionService: Oturum baslatma - Kullanici ogrenci degil - ID: " + studentId + ", Rol: " + student.getRole());
             throw new UserNotAuthorizedException("ID " + studentId + " olan kullanıcı bir öğrenci değil. Sadece öğrenciler quiz oturumu başlatabilir.");
        }

        // Quizin aktif ve çözülebilir olup olmadığını kontrol etma (tarih/saat ve isActive alanları)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime quizStartDate = quiz.getStartDate() != null ? quiz.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        LocalDateTime quizEndDate = quiz.getEndDate() != null ? quiz.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;

        if (!quiz.isActive() ||
            (quizStartDate != null && now.isBefore(quizStartDate)) ||
            (quizEndDate != null && now.isAfter(quizEndDate))) {
            System.err.println("QuizSessionService: Oturum baslatma - Quiz aktif degil veya suresi dolmus - Quiz ID: " + quizId);
            throw new QuizNotAvailableException("ID " + quizId + " olan quiz şu anda çözülebilir durumda değil.");
        }

        
         quizSessionRepository.findByStudentAndQuiz(student, quiz).ifPresent(existingSession -> {
              System.err.println("QuizSessionService: Oturum baslatma - Ogrenci quizi zaten cozmussu - Ogrenci ID: " + studentId + ", Quiz ID: " + quizId);
              throw new QuizAlreadyTakenException("ID " + studentId + " olan öğrenci bu quizi zaten çözmüş.");
         });
        // Bu kontrol için QuizSessionRepository'de "Optional<QuizSession> findByStudentAndQuiz(User student, Quiz quiz);" metodunu tanımlamanız gerekir.


        // yeni QuizSession objesi oluşturma (başlangıç zamanını constructor'da set ediyoruz)
        QuizSession newSession = new QuizSession(student, quiz);

        // kaydedip döndürme
        QuizSession savedSession = quizSessionRepository.save(newSession);

        System.out.println("QuizSessionService: Quiz oturumu baslatildi - Oturum ID: " + savedSession.getId());
        return savedSession;
    }

 // Cevap Gönderme/Kaydetme (Student yetkisi ve aktif oturum gerektirecek)
    // Sizin QuestionAnswer template'indeki answer ve isCorrect alanlarının kaydedilmesi burada olacak.
    @Transactional // Cevap kaydetme bir transaction içinde olmalı
    public AnswerAttempt submitAnswer(int sessionId, int questionId, String submittedAnswerText, Set<Integer> selectedOptionIds, int submitterUserId) {
        System.out.println("QuizSessionService: Cevap gonderme istegi - Oturum ID: " + sessionId + ", Soru ID: " + questionId);

        // sessionId ile aktif QuizSession'ı bulma (Bulunamazsa QuizSessionNotFoundException fırlat)
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Cevap gonderme - Oturum bulunamadi - ID: " + sessionId);
                    return new QuizSessionNotFoundException("ID " + sessionId + " olan quiz oturumu bulunamadı.");
                });

        // Gelen kullanıcının (submitterUserId) bulunan oturumun sahibi (session.getStudent().getId()) olup olmadığını kontrol et <-- BURASI EKLENDİ
        if (session.getStudent() == null || session.getStudent().getId() != submitterUserId) {
            System.err.println("QuizSessionService: Cevap gonderme - Kullanici oturum sahibi degil veya ogrenci bilgisi eksik - Oturum ID: " + sessionId + ", Gonderen ID: " + submitterUserId);
            throw new UserNotAuthorizedException("ID " + submitterUserId + " olan kullanıcının ID " + sessionId + " olan oturuma cevap gönderme yetkisi yok.");
        }

        // Oturumun hala aktif olup olmadığını kontrol et (bitiş zamanı set edilmişse tamamlanmıştır)
        if (session.getEndTime() != null) {
             System.err.println("QuizSessionService: Cevap gonderme - Oturum zaten tamamlanmis - ID: " + sessionId);
             // Zaten tamamlanmış bir oturuma cevap gönderilemez
             throw new QuizSessionExpiredException("ID " + sessionId + " olan quiz oturumu zaten tamamlanmış."); // Veya yeni exception
        }

        // Oturumun süresinin dolup dolmadığını kontrol et (Quiz Entity'deki duration alanına göre)
         Quiz quiz = session.getQuiz(); // Oturumun quizi
         Integer duration = (quiz != null) ? quiz.getDuration() : null; // Get duration safely
         if (quiz != null && duration != null && session.getStartTime() != null) { // Check the local variable
              LocalDateTime sessionEndTimeLimit = session.getStartTime().plusMinutes(duration); // Use the local variable (unboxed here)
              if (LocalDateTime.now().isAfter(sessionEndTimeLimit)) {
                   System.err.println("QuizSessionService: Cevap gonderme - Oturum suresi dolmus - ID: " + sessionId);
                   // Oturum süresi dolmuşsa otomatik tamamlama yapılabilir veya hata fırlatılabilir.
                   // completeQuizSession metodunun da yetki kontrolü yapması gerekecek veya burada çağırıldığı için yetki kontrolü atlanabilir.
                   // Basitlik adına, süresi dolmuşsa bu isteği reddedip kullanıcıya oturumu tamamlaması gerektiğini söyleyebiliriz.
                   // completeQuizSession(sessionId); // Oturumu otomatik tamamlama yerine hata fırlatmayı tercih edelim
                   throw new QuizSessionExpiredException("ID " + sessionId + " olan quiz oturum süreniz dolmuştur. Lütfen oturumu tamamlayın.");
              }
         }


        // questionId ile Soruyu bulma (Bulunamazsa QuestionNotFoundException fırlat)
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Cevap gonderme - Soru bulunamadi - ID: " + questionId);
                    return new QuestionNotFoundException("ID " + questionId + " olan soru bulunamadı.");
                });

        // Soru gerçekten bu quize mi ait? (quizId yerine session'ın quizini kontrol et)
        // Bu kontrol aslında submitAnswer metodunun parametresi olarak quizId almadığı için Service içinde yapılmalıdır.
        // submitAnswer sadece sessionId ve questionId alıyor. Question Entity'sinin Quiz ilişkisi üzerinden kontrol ediyoruz.
        if (question.getQuiz() == null || session.getQuiz().getId() != question.getQuiz().getId()) {
           System.err.println("QuizSessionService: Cevap gonderme - Soru oturumun quizine ait degil - Soru ID: " + questionId + ", Oturum Quiz ID: " + session.getQuiz().getId());
           throw new QuestionDoesNotBelongToQuizException("Soru ID " + questionId + " , Quiz oturumu ID " + sessionId + "'nin quizine ait değil.");
        }


        // Daha önce bu soru için cevap var mı kontrolü AnswerAttemptRepository metodunu kullanarak
        answerAttemptRepository.findByQuizSessionAndQuestion(session, question).ifPresent(existingAttempt -> {
             System.err.println("QuizSessionService: Cevap gonderme - Soruya zaten cevap verilmis - Oturum ID: " + sessionId + ", Soru ID: " + questionId);
             throw new AnswerAlreadySubmittedException("ID " + questionId + " olan soruya zaten cevap verilmiş.");
        });


        // AnswerAttempt objesi oluşturma
        AnswerAttempt newAttempt;
        QuestionType questionType = question.getType();

        if (questionType == null) {
             System.err.println("QuizSessionService: Cevap gonderme - Soru tipi bulunamadi veya null - Soru ID: " + questionId);
             throw new IllegalStateException("Soru ID " + questionId + " için soru tipi tanımlı değil."); // Beklenmeyen durum
        }

        // Soru tipine göre cevabı AnswerAttempt'e set et ve validasyon yap
        if ("Açık Uçlu".equals(questionType.getTypeName()) || "Kısa Cevap".equals(questionType.getTypeName())) {
            // Metin cevabı bekleniyor, şık seçilmemeli
            if (selectedOptionIds != null && !selectedOptionIds.isEmpty()) {
                 System.err.println("QuizSessionService: Cevap gonderme - Metin sorusu icin secili sik gonderildi - Soru ID: " + questionId);
                 throw new InvalidQuestionTypeForAnswerException("Metin tabanlı soru için şık gönderilemez.");
            }
            // submittedAnswerText null veya boş olabilir, bu geçerli bir boş cevap olabilir.
            newAttempt = new AnswerAttempt(session, question, submittedAnswerText); // Metin cevabı ile oluştur

        } else if ("Çoktan Seçmeli".equals(questionType.getTypeName())) {
            // Şık seçimi bekleniyor, metin cevabı verilmemeli
             if (submittedAnswerText != null && !submittedAnswerText.trim().isEmpty()) {
                 System.err.println("QuizSessionService: Cevap gonderme - Coktan secmeli soru icin metin cevap gonderildi - Soru ID: " + questionId);
                 throw new InvalidQuestionTypeForAnswerException("Çoktan seçmeli soru için metin cevap gönderilemez.");
             }
             // Çoktan seçmeli sorular için şık seçimi boş olamaz (en az 1 şık seçilmeli varsayıyoruz)
             if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
                 System.err.println("QuizSessionService: Cevap gonderme - Coktan secmeli soru icin secili sik gonderilmedi - Soru ID: " + questionId);
                 // Çoktan seçmeli sorularda en az bir şık seçimi zorunlu olabilir.
                 // İş kuralınıza göre burayı düzenleyebilirsiniz.
                 throw new InvalidQuestionTypeForAnswerException("Çoktan seçmeli soru için en az bir şık seçilmelidir.");
             }

            Set<Option> selectedOptions = new HashSet<>();
            selectedOptionIds.forEach(optionId -> {
                 // Seçilen şık Entity'sini bul (Bulunamazsa OptionNotFoundException fırlat)
                 Option option = optionRepository.findById(optionId)
                         .orElseThrow(() -> {
                              System.err.println("QuizSessionService: Cevap gonderme - Secilen sik bulunamadi - Option ID: " + optionId);
                              return new OptionNotFoundException("ID " + optionId + " olan şık bulunamadı.");
                         });
                 // Seçilen şık bu soruya mı ait kontrolü
                 if (option.getQuestion() == null || option.getQuestion().getId() != questionId) {
                     System.err.println("QuizSessionService: Cevap gonderme - Secilen sik soruya ait degil - Sik ID: " + optionId + ", Soru ID: " + questionId);
                     throw new InvalidOptionForQuestionException("Şık ID " + optionId + " , Soru ID " + questionId + "'e ait değil.");
                 }
                 selectedOptions.add(option);
            });
            newAttempt = new AnswerAttempt(session, question, selectedOptions); // Seçili şıklar ile oluştur

        } else {
            System.err.println("QuizSessionService: Cevap gonderme - Bilinmeyen veya desteklenmeyen soru tipi - Soru ID: " + questionId + ", Tip: " + questionType.getTypeName());
            throw new InvalidQuestionTypeForAnswerException("Bilinmeyen veya desteklenmeyen soru tipi."); // Hata yönetimi
        }

        // Cevabın doğruluğunu kontrol et (checkAnswerCorrectness metodunu çağır)
        boolean isCorrect = checkAnswerCorrectness(question, newAttempt); // checkAnswerCorrectness metodu aşağıda

        // isCorrect flag'ini AnswerAttempt'e set et
        newAttempt.setCorrect(isCorrect);

        // AnswerAttempt'ı QuizSession'a ekleme (QuizSession Entity'deki addAnswerAttempt metodu ilişkiyi kurar)
        session.addAnswerAttempt(newAttempt);

        // QuizSession'ı kaydet (AnswerAttempt'in cascade = CascadeType.ALL ile kaydedilmesi beklenir)
        quizSessionRepository.save(session);

        System.out.println("QuizSessionService: Cevap basariyla kaydedildi - Attempt ID: " + newAttempt.getId() + ", Dogru Mu: " + newAttempt.isCorrect());
        return newAttempt; // Kaydedilen AnswerAttempt'i döndür
    }



    // --- Yardımcı Metotlar ---

    
 // Cevabın doğruluğunu kontrol etme (Internal yardımcı metot)
    // Sizin QuestionAnswer template'indeki checkAnswer() metodunun mantığı burada.
    // Bu metodun iş mantığı, soru tipine (QuestionType) ve Question Entity'sindeki doğru cevaba göre değişir.
    private boolean checkAnswerCorrectness(Question question, AnswerAttempt answerAttempt) {
        System.out.println("QuizSessionService: Cevap dogrulugu kontrol ediliyor - Soru ID: " + question.getId() + ", Attempt ID: " + answerAttempt.getId());

        // Question Entity'sinin yüklü olduğundan ve Type ilişkisinin geldiğinden emin olun.
        // submitAnswer metodunda questionRepository.findById kullandığımız için genellikle Type ilişkisi de FetchType.EAGER veya JOIN FETCH ile gelmelidir.
        QuestionType questionType = question.getType();
         if (questionType == null) {
             System.err.println("QuizSessionService: checkAnswerCorrectness - Soru tipi yuklenemedi veya null - Soru ID: " + question.getId());
             // Soru tipi yoksa doğruluğu kontrol edemeyiz.
             return false; // Beklenmeyen durum, cevabı doğru kabul etme
         }

        String typeName = questionType.getTypeName();

        if ("Açık Uçlu".equals(typeName) || "Kısa Cevap".equals(typeName)) {
            // Metin tabanlı sorular: Gönderilen metni doğru cevap metniyle karşılaştır
            // question.getAnswer().getAnswerText() ve answerAttempt.getSubmittedAnswerText() null olabilir!
            QuestionAnswer correctAnswerEntity = question.getAnswer();
            String correctText = (correctAnswerEntity != null) ? correctAnswerEntity.getAnswer() : null;
            String submittedText = answerAttempt.getSubmittedAnswerText();

            // Hem doğru cevap hem de gönderilen cevap boş veya null ise, bu genellikle yanlış kabul edilir.
            // Sadece doğru cevap metni boş veya null değilse ve gönderilen metin tam eşleşiyorsa doğru kabul edelim.
            if (correctText == null || correctText.trim().isEmpty()) {
                 // Soru için tanımlanmış doğru cevap metni yoksa
                 System.out.println("QuizSessionService: checkAnswerCorrectness - Metin tabanli soru (Acik Uclu/Kisa Cevap) - Dogru cevap metni tanimli degil. ID: " + question.getId());
                 return false; // Tanımlı doğru cevap yoksa cevap doğru olamaz
            }
            if (submittedText == null || submittedText.trim().isEmpty()) {
                 // Öğrenci boş cevap göndermişse
                 System.out.println("QuizSessionService: checkAnswerCorrectness - Metin tabanli soru (Acik Uclu/Kisa Cevap) - Gonderilen cevap metni bos. ID: " + question.getId());
                 return false; // Boş cevap doğru olamaz (doğru cevap metni boş değilse)
            }


            // Karşılaştırma (büyük/küçük harf ve baştaki/sondaki boşlukları dikkate almadan)
            boolean isMatch = submittedText.trim().equalsIgnoreCase(correctText.trim());
             System.out.println("QuizSessionService: checkAnswerCorrectness - Metin tabanli soru (Acik Uclu/Kisa Cevap) - ID: " + question.getId() + ", Dogru Cevap: '" + correctText + "', Gonderilen Cevap: '" + submittedText + "', Eslesme: " + isMatch);
            return isMatch;

        } else if ("Çoktan Seçmeli".equals(typeName)) {
            // Çoktan seçmeli sorular: Öğrencinin seçtiği şıkları doğru şıklarla karşılaştır
            Set<Option> selectedOptions = answerAttempt.getSelectedOptions(); // Öğrencinin seçtikleri Set'i
            Set<Option> correctOptions = new HashSet<>(); // Sorunun doğru şıkları Set'i

            // Question'ın doğru şıklarını bul ve Set'e topla
            if (question.getOptions() != null) {
                question.getOptions().stream()
                        .filter(Option::isCorrect)
                        .forEach(correctOptions::add);
            }

             // Seçilen şık seti ile doğru şık setinin birebir aynı olup olmadığını kontrol et
             // Her iki setin eleman sayısı aynı olmalı VE bir setteki tüm elemanlar diğer sette de bulunmalı.
             // Set.equals() metodu bu kontrolü yapar.
             // Eğer selectedOptions null ise equals metodu false döner, bu da doğru.
             boolean isMatch = selectedOptions != null && selectedOptions.equals(correctOptions);

             System.out.println("QuizSessionService: checkAnswerCorrectness - Coktan secmeli soru - ID: " + question.getId() + ", Eslesme: " + isMatch);
            return isMatch;

        } else {
            // Bilinmeyen veya desteklenmeyen soru tipi
            System.err.println("QuizSessionService: checkAnswerCorrectness - Bilinmeyen veya desteklenmeyen soru tipi - ID: " + question.getId() + ", Tip: " + typeName);
            return false; // Bilinmeyen tip, cevabı doğru kabul etme
        }
    }

 // Quiz Oturumunun Puanını Hesaplama (Internal yardımcı metot)
    @Deprecated
    private int calculateScore(QuizSession session) {
        System.out.println("QuizSessionService: Puan hesaplaniyor - Oturum ID: " + session.getId());
        int totalScore = 0;

        // Oturumun cevapları üzerinden döngü
        // Dikkat: session.getAnswers() ilişkisi eğer FetchType.LAZY ise ve daha önce eagerly getirilmemişse,
        // bu döngü N+1 sorgu problemine yol açabilir (her attempt için soru/şık detayları yükleniyorsa).
        // Performans kritikse, QuizSessionRepository'de AnswerAttempt'leri JOIN FETCH ile getiren özel bir metot kullanılabilir.
        List<AnswerAttempt> attempts = session.getAnswers(); // Cevap listesini al

        if (attempts != null && !attempts.isEmpty()) {
             for (AnswerAttempt attempt : attempts) {
                // AnswerAttempt doğru işaretlenmişse puan ekle
                if (attempt.isCorrect()) {
                    // Varsayım: Her doğru soru için sabit 10 puan veriyoruz.
                
                    totalScore += 10; // Her doğru cevap için sabit 10 puan
                }
            }
        } else {
             System.out.println("QuizSessionService: Puan hesaplaniyor - Oturum ID: " + session.getId() + ", Cevap bulunamadi.");
        }


        System.out.println("QuizSessionService: Puan hesaplandi - Oturum ID: " + session.getId() + ", Hesaplanan Puan: " + totalScore);
        return totalScore;
    }
 // Quiz Oturum Süresini Hesaplama (Sizin TakeQuiz template'indeki calculateDuration)
    @Transactional(readOnly = true) // Sadece okuma işlemi
    public Duration calculateDuration(int sessionId) { // Parametreyi sessionId olarak değiştirelim
        System.out.println("QuizSessionService: Sure hesaplaniyor - Oturum ID: " + sessionId);

         // Oturumu getir (EndTime ve StartTime lazım)
         QuizSession session = quizSessionRepository.findById(sessionId)
                 .orElseThrow(() -> {
                     System.err.println("QuizSessionService: Sure hesaplaniyor - Oturum bulunamadi - ID: " + sessionId);
                     return new QuizSessionNotFoundException("ID " + sessionId + " olan quiz oturumu bulunamadı.");
                 });


         if (session.getStartTime() == null) {
             System.err.println("QuizSessionService: Sure hesaplaniyor - Baslangic zamani bos - Oturum ID: " + sessionId);
             // Başlangıç zamanı olmadan süre hesaplanamaz, 0 dönebilir veya hata fırlatılabilir.
             // Hata yerine 0 dönmek API kullanıcısı için daha anlamlı olabilir.
             return Duration.ZERO;
         }

         // Bitiş zamanı set edilmişse o zamanı kullan (tamamlanmış oturum),
         // yoksa şu anki zamanı al (devam eden oturumlar için)
         LocalDateTime endTime = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now();

         // Başlangıç zamanı ile bitiş zamanı arasındaki süreyi hesapla
         Duration duration = Duration.between(session.getStartTime(), endTime);

         System.out.println("QuizSessionService: Sure hesaplandi - Oturum ID: " + sessionId + ", Sure: " + duration);
         return duration;
    }
    
    @Transactional(readOnly = true)
    public QuizSession getQuizSessionDetails(int sessionId, int viewerUserId) { // <-- viewerUserId eklendi, dönüş tipi QuizSession oldu
        System.out.println("QuizSessionService: Oturum detaylari getiriliyor - Oturum ID: " + sessionId + ", Izleyen ID: " + viewerUserId);

        // sessionId ile QuizSession'ı bulma (Bulunamazsa QuizSessionNotFoundException fırlat)
        // Artık Optional dönmüyor, doğrudan exception fırlatıyor.
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Oturum detaylari getirirken - Oturum bulunamadi - ID: " + sessionId);
                    return new QuizSessionNotFoundException("ID " + sessionId + " olan quiz oturumu bulunamadı.");
                });

        // viewerUserId ile Kullanıcıyı bulma (Bulunamazsa UserNotFoundException fırlat - Security handle etmeli ama yine de Service'te kontrol iyi olabilir)
        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Oturum detaylari getirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    // Bu hata normalde Security katmanında yakalanmalı, buraya düşerse beklenmedik bir durumdur.
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        // Yetki Kontrolü: Kullanıcı oturumun sahibi mi, quizin öğretmeni mi, yoksa Admin mi?
        boolean isSessionOwner = session.getStudent() != null && session.getStudent().getId() != viewerUserId;
        boolean isQuizTeacher = session.getQuiz() != null && session.getQuiz().getTeacher() != null && session.getQuiz().getTeacher().getId() != viewerUserId;
        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole()); // Varsayım: User Entity'de getRole() metodu var

        if (!isSessionOwner && !isQuizTeacher && !isAdmin) {
            System.err.println("QuizSessionService: Oturum detaylari getirirken - Kullanici yetkisiz - Oturum ID: " + sessionId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("ID " + viewerUserId + " olan kullanıcının bu oturumun detaylarını görme yetkisi yok.");
        }

        // Yetkili ise oturumu döndür
        System.out.println("QuizSessionService: Oturum detaylari bulundu ve yetki kontrolu gecti - Oturum ID: " + sessionId);
        return session;
    }

    /**
     * Öğrencinin bir soruya verdiği cevabı kaydeder
     * 
     * @param sessionId Oturum ID'si
     * @param studentId Öğrenci ID'si
     * @param answerDto Cevap bilgilerini içeren DTO
     * @return Başarılı olup olmadığı
     */
    @Transactional
    public boolean saveAnswer(int sessionId, int studentId, StudentAnswerDto answerDto) {
        // Oturumu bul
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new QuizSessionNotFoundException("ID " + sessionId + " olan quiz oturumu bulunamadı."));
        
        // Öğrencinin oturumun sahibi olup olmadığını kontrol et
        if (session.getStudent() == null || session.getStudent().getId() != studentId) {
            throw new UserNotAuthorizedException("Bu oturumun sahibi değilsiniz.");
        }
        
        // Oturumun tamamlanmış olup olmadığını kontrol et
        if (session.isSubmitted()) {
            throw new QuizSessionCompletedException("Bu quiz oturumu zaten tamamlanmış.");
        }
        
        // Soruyu bul
        Question question = questionRepository.findById(answerDto.getQuestionId())
                .orElseThrow(() -> new QuestionNotFoundException("ID " + answerDto.getQuestionId() + " olan soru bulunamadı."));
        
        // Cevap denemesi oluştur
        AnswerAttempt attempt = new AnswerAttempt();
        attempt.setQuestion(question);
        attempt.setQuizSession(session);
        
        // Cevap tipine göre işlem yap
        if (answerDto.getAnswerType() == AnswerType.MULTIPLE_CHOICE) {
            // Çoktan seçmeli soru ise seçenek ID'sini kaydet
            if (answerDto.getSelectedOptionIds() != null && !answerDto.getSelectedOptionIds().isEmpty()) {
                // Tüm doğru şıkları al
                Set<Option> correctOptions = question.getOptions().stream()
                        .filter(Option::isCorrect)
                        .collect(Collectors.toSet());
                
                // Seçilen şıkları bul ve ekle
                Set<Option> selectedOptions = new HashSet<>();
                for (Integer optionId : answerDto.getSelectedOptionIds()) {
                    Option selectedOption = optionRepository.findById(optionId)
                            .orElseThrow(() -> new RuntimeException("Seçenek bulunamadı: " + optionId));
                    selectedOptions.add(selectedOption);
                    attempt.addSelectedOption(selectedOption);
                }
                
                // Tüm doğru şıklar seçilmiş ve başka şık seçilmemişse doğru kabul et
                boolean isCorrect = selectedOptions.containsAll(correctOptions) && 
                                  selectedOptions.size() == correctOptions.size();
                attempt.setCorrect(isCorrect);
                attempt.setEarnedPoints(isCorrect ? question.getPoints() : 0);
            }
        } else if (answerDto.getAnswerType() == AnswerType.TEXT) {
            // Açık uçlu soru ise metin cevabını kaydet
            attempt.setTextAnswer(answerDto.getTextAnswer());
            
            // AI tarafından değerlendirilmiş açık uçlu cevap
            if (answerDto.getScore() > 0) {
                // AI skoru ve kazanılan puanı ayarla
                attempt.setAiScore((int) answerDto.getScore());
                attempt.setEarnedPoints((int) answerDto.getScore());
                
                // Doğru olarak işaretle (puan kazandıysa)
                attempt.setCorrect(true);
                
                // AI açıklamasını kaydet
                if (answerDto.getAiExplanation() != null) {
                    attempt.setAiExplanation(answerDto.getAiExplanation());
                    System.out.println("QuizSessionService: AI açıklaması kaydedildi: " + answerDto.getAiExplanation());
                }
            } else {
                // Puan kazanmadıysa
                attempt.setEarnedPoints(0);
                attempt.setCorrect(false);
            }
        }
        
        // Cevap denemesini kaydet
        answerAttemptRepository.save(attempt);
        
        return true;
    }
    
    /**
     * Soru ID'sine göre soruyu getirir
     * 
     * @param questionId Soru ID'si
     * @return Soru nesnesi
     */
    @Transactional(readOnly = true)
    public Question getQuestionById(int questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("ID " + questionId + " olan soru bulunamadı."));
    }
    
    // Bir Öğrencinin Tüm Oturumlarını Getirme
    @Transactional(readOnly = true) // Sadece okuma işlemi
    public List<QuizSession> getStudentQuizSessions(int studentId) {
        System.out.println("QuizSessionService: Öğrenci oturumlari getiriliyor - Öğrenci ID: " + studentId);

        // studentId ile Öğrenci kullanıcısını bulma (Bulunamazsa UserNotFoundException fırlat)
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Ogrenci oturumlarini getirirken - Ogrenci bulunamadi - ID: " + studentId);
                    return new UserNotFoundException("ID " + studentId + " olan öğrenci bulunamadı.");
                });

        // Bu kullanıcının rolünün STUDENT olduğunu kontrol etme (Öğrenci değilse UserNotAuthorizedException fırlat)
         if (!"ROLE_STUDENT".equals(student.getRole())) {
             System.err.println("QuizSessionService: Ogrenci oturumlarini getirirken - Kullanici ogrenci degil - ID: " + studentId + ", Rol: " + student.getRole());
             throw new UserNotAuthorizedException("ID " + studentId + " olan kullanıcı bir öğrenci değil.");
         }

        // Öğrenciye ait tüm oturumları repository metodu ile bul ve döndür
        // QuizSessionRepository'de findByStudent(User student) metodumuz zaten var.
        List<QuizSession> sessions = quizSessionRepository.findAllByStudent(student);

        System.out.println("QuizSessionService: Ogrenci ID " + studentId + " icin " + sessions.size() + " adet oturum bulundu.");
        return sessions;
    }

}