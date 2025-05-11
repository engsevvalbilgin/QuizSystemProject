package com.example.QuizSystemProject.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.QuizSystemProject.Repository.*;import com.example.QuizSystemProject.Model.*;
 // Paket adınızın doğru olduğundan emin olun

import com.example.QuizSystemProject.Model.*; // Model katmanındaki tüm Entity'leri import edin
import com.example.QuizSystemProject.Repository.*; // Repository katmanındaki tüm Repository'leri import edin
import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için
import org.springframework.stereotype.Service; // Service anotasyonunu import edin

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
    @Autowired
    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository,
                       OptionRepository optionRepository, UserRepository userRepository,
                       QuestionTypeRepository questionTypeRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.userRepository = userRepository;
        this.questionTypeRepository = questionTypeRepository;
    }

    // --- Sizin template'lerinizdeki ilgili işlevlere karşılık gelen metot imzaları ---

    // Quiz Oluşturma (Teacher yetkisi gerektirecek)
    // Sizin Quiz.java template'indeki createQuiz ve Teacher.java template'indeki createQuiz metotlarının mantığı burada birleşiyor.
    public Quiz createQuiz(Long teacherId, String name, String description, Integer durationMinutes) {
        // NOT: Gerçek implementasyonda, teacherId ile Teacher kullanıcısını bulma,
        // bu kullanıcının rolünün TEACHER olduğunu kontrol etme, Quiz objesi oluşturma,
        // kaydedip döndürme gibi mantıklar olacak. Tarihler burada veya Controller'dan set edilerek gelebilir.

        System.out.println("QuizService: Quiz olusturma başlatıldı - Ogretmen ID: " + teacherId + ", Ad: " + name);
        // Placeholder
        // Optional<User> teacherOptional = userRepository.findById(teacherId);
        // if (teacherOptional.isPresent() && "ROLE_TEACHER".equals(teacherOptional.get().getRole())) {
        //     User teacher = teacherOptional.get();
        //     Quiz newQuiz = new Quiz(name, teacher, null, null, durationMinutes, description); // Başlangıç/Bitiş tarihleri şimdilik null
        //     return quizRepository.save(newQuiz);
        // }
        // throw new RuntimeException("Ogretmen bulunamadi veya yetkisi yok!"); // Hata yönetimi

        return new Quiz(); // Şimdilik placeholder
    }

    // Quiz Güncelleme (Teacher yetkisi gerektirecek)
    // Sizin Quiz.java template'indeki updateQuiz metodunun mantığı burada.
    public Quiz updateQuiz(Long quizId, String name, String description, Integer durationMinutes, boolean isActive) {
        // NOT: Gerçek implementasyonda, quizId ile Quizi bulma, güncel bilgileri set etme,
        // kaydedip döndürme gibi mantıklar olacak.

        System.out.println("QuizService: Quiz güncelleme başlatıldı - Quiz ID: " + quizId);
        // Placeholder
        // Optional<Quiz> quizOptional = quizRepository.findById(quizId);
        // if (quizOptional.isPresent()) {
        //     Quiz quizToUpdate = quizOptional.get();
        //     // Güncel bilgileri set et
        //     quizToUpdate.setName(name);
        //     quizToUpdate.setDescription(description);
        //     quizToUpdate.setDurationMinutes(durationMinutes);
        //     quizToUpdate.setActive(isActive);
        //     // Kaydet ve döndür
        //     return quizRepository.save(quizToUpdate);
        // }
        // throw new RuntimeException("Quiz bulunamadi!"); // Hata yönetimi

        return new Quiz(); // Şimdilik placeholder
    }

    // Quiz Silme (Teacher yetkisi gerektirecek)
    // Sizin Quiz.java template'indeki deleteQuiz metodunun mantığı burada.
    public void deleteQuiz(Long quizId) {
        // NOT: Gerçek implementasyonda, quizId ile Quizi bulma, silme (veya isActive=false yapma)
        // gibi mantıklar olacak.

        System.out.println("QuizService: Quiz silme başlatıldı - Quiz ID: " + quizId);
        // Placeholder
        // quizRepository.deleteById(quizId);
    }

    // Tüm Quizleri Getirme (Herkes görebilir veya filtrelenebilir)
    public List<Quiz> getAllQuizzes() {
        System.out.println("QuizService: Tüm quizler getiriliyor.");
        // Placeholder
        // return quizRepository.findAll();
        return List.of(new Quiz()); // Simülasyon
    }

     // ID'ye Göre Quiz Getirme
    public Optional<Quiz> getQuizById(Long quizId) {
        System.out.println("QuizService: Quiz getiriliyor - ID: " + quizId);
         // Placeholder
         // return quizRepository.findById(quizId);
         return Optional.empty(); // Simülasyon
    }

    // Bir Öğretmenin Quizlerini Getirme
     public List<Quiz> getQuizzesByTeacher(Long teacherId) {
         System.out.println("QuizService: Öğretmen quizleri getiriliyor - Ogretmen ID: " + teacherId);
         // Placeholder
         // Optional<User> teacherOptional = userRepository.findById(teacherId);
         // if (teacherOptional.isPresent() && "ROLE_TEACHER".equals(teacherOptional.get().getRole())) {
         //     return quizRepository.findAllByTeacher(teacherOptional.get());
         // }
         // return List.of(); // Öğretmen bulunamadıysa boş liste döndür

          return List.of(new Quiz()); // Simülasyon
     }


    // --- Soru Yönetimi (Teacher yetkisi gerektirecek) ---
    // Sizin Question.java template'indeki createQuestion, deleteQuestion, updateQuestion
    // ve Quiz.java template'indeki addQuestion, removeQuestion, updateQuestion metotlarının mantığı burada birleşiyor.

    // Quize Yeni Soru Ekleme
    public Question addQuestionToQuiz(Long quizId, int number, String questionSentence, String correctAnswerText, Long questionTypeId, List<Option> options) {
         // NOT: Gerçek implementasyonda, quizId ile Quizi bulma, questionTypeId ile QuestionType'ı bulma,
         // Question objesi oluşturma, Option'ları ilişkilendirme, Question'ı Quize ekleme (Quiz Entity'deki addQuestion metodu ile),
         // kaydedip döndürme gibi mantıklar olacak.

        System.out.println("QuizService: Quize soru ekleme başlatıldı - Quiz ID: " + quizId);
        // Placeholder
        // Optional<Quiz> quizOptional = quizRepository.findById(quizId);
        // Optional<QuestionType> typeOptional = questionTypeRepository.findById(questionTypeId);
        //
        // if (quizOptional.isPresent() && typeOptional.isPresent()) {
        //    Quiz quiz = quizOptional.get();
        //    QuestionType type = typeOptional.get();
        //    Question newQuestion = new Question(number, questionSentence, correctAnswerText, quiz, type);
        //
        //    if (options != null && !options.isEmpty()) {
        //        options.forEach(option -> newQuestion.addOption(option)); // Şıkları soruya ekle
        //    }
        //
        //    quiz.addQuestion(newQuestion); // Soru yu quize ekle (Quiz Entity'deki yardımcı metot)
        //    quizRepository.save(quiz); // Quizi kaydet (Question ve Optionlar cascade ile kaydedilir)
        //    return newQuestion;
        // }
         // throw new RuntimeException("Quiz veya Soru Tipi bulunamadi!"); // Hata yönetimi


        return Question.createQuestion(); // Şimdilik; // Şimdilik placeholder
    }

    // Quizden Soru Silme
    public void removeQuestionFromQuiz(Long quizId, Long questionId) {
         // NOT: Gerçek implementasyonda, quizId ile Quizi, questionId ile Soruyu bulma,
         // Soruyu Quiz'in listesinden çıkarma (Quiz Entity'deki removeQuestion metodu ile),
         // Quizi kaydetme (Question ve Optionlar orphanRemoval sayesinde silinir) gibi mantıklar olacak.

        System.out.println("QuizService: Quizden soru silme başlatıldı - Quiz ID: " + quizId + ", Soru ID: " + questionId);
        // Placeholder
        // Optional<Quiz> quizOptional = quizRepository.findById(quizId);
        // Optional<Question> questionOptional = questionRepository.findById(questionId);
        //
        // if (quizOptional.isPresent() && questionOptional.isPresent()) {
        //    Quiz quiz = quizOptional.get();
        //    Question questionToRemove = questionOptional.get();
        //
        //    // Soru bu quize ait mi kontrolü de eklenebilir
        //    if (quiz.getQuestions().contains(questionToRemove)) {
        //        quiz.removeQuestion(questionToRemove); // Soru yu quizden çıkar (Quiz Entity'deki yardımcı metot)
        //        quizRepository.save(quiz); // Quizi kaydet
        //    } else {
        //        throw new RuntimeException("Soru bu quize ait degil!");
        //    }
        // }
         // throw new RuntimeException("Quiz veya Soru bulunamadi!"); // Hata yönetimi
    }

     // Quizdeki Soruyu Güncelleme
     public Question updateQuestionInQuiz(int quizId, int questionId, int number, String questionSentence, String correctAnswerText, Long questionTypeId, List<Option> options) {
          // NOT: Gerçek implementasyonda, quizId ve questionId ile ilgili objeleri bulma,
          // Soru üzerindeki alanları (number, text, answerText, type) güncelleme,
          // Şıkları (Option) yönetme (eski şıkları silip yenilerini ekleme veya güncelleme),
          // kaydedip döndürme gibi mantıklar olacak.

         System.out.println("QuizService: Quizdeki soruyu güncelleme başlatıldı - Quiz ID: " + quizId + ", Soru ID: " + questionId);
          // Placeholder
          // Optional<Question> questionOptional = questionRepository.findById(questionId);
          // Optional<QuestionType> typeOptional = questionTypeRepository.findById(questionTypeId);
          //
          // if (questionOptional.isPresent() && typeOptional.isPresent()) {
          //     Question questionToUpdate = questionOptional.get();
          //     QuestionType type = typeOptional.get();
          //
          //     // Güncel bilgileri set et
          //     questionToUpdate.setNumber(number);
          //     questionToUpdate.setQuestionSentence(questionSentence);
          //     questionToUpdate.setCorrectAnswerText(correctAnswerText);
          //     questionToUpdate.setType(type); // Soru tipini güncelle
          //
          //     // Şıkları yönetme (Bu kısım çoktan seçmeli sorular için daha karmaşıktır ve OptionService gibi ayrı bir servis düşünülse daha iyi olabilir)
          //     // Basitçe: Mevcut şıkları silip, gelen yeni şıkları ekleyelim (orphanRemoval sayesinde eski şıklar db'den silinir)
          //     questionToUpdate.getOptions().clear(); // Mevcut şıkları temizle
          //     if (options != null) {
          //         options.forEach(option -> questionToUpdate.addOption(option)); // Yeni şıkları ekle
          //     }
          //
          //     return questionRepository.save(questionToUpdate); // Soruyu kaydet (şıkları da cascade ile günceller/kaydeder)
          // }
          // throw new RuntimeException("Soru veya Soru Tipi bulunamadi!"); // Hata yönetimi

         return Question.createQuestion();
     }


    // --- Option Yönetimi (Teacher yetkisi gerektirecek - Genellikle soru yönetimi içinde yapılır ama ayrı metotlar da olabilir) ---
    // Sizin Option.java template'indeki createOption, deleteOption, updateOption metotlarının mantığı burada veya QuestionService içinde olur.

    // Bir Soruya Şık Ekleme (Örnek: Çoktan seçmeli soruya şık ekleme)
     public Option addOptionToQuestion(Long questionId, String text, boolean isCorrect) {
          // NOT: Gerçek implementasyonda, questionId ile Soruyu bulma, Option objesi oluşturma,
          // Option'ı soruya ekleme (Question Entity'deki addOption metodu ile),
          // Soruyu kaydetme (Option cascade ile kaydedilir) gibi mantıklar olacak.

         System.out.println("QuizService: Soruya şık ekleme başlatıldı - Soru ID: " + questionId);
          // Placeholder
          // Optional<Question> questionOptional = questionRepository.findById(questionId);
          // if (questionOptional.isPresent()) {
          //     Question question = questionOptional.get();
          //     // Soru tipinin çoktan seçmeli olup olmadığını kontrol etmek iyi practice'dir
          //     if ("Çoktan Seçmeli".equals(question.getType().getTypeName())) { // Veya Enum kullanıyorsak type == QuestionType.MULTIPLE_CHOICE
          //         Option newOption = new Option(text, isCorrect, question); // Option objesi oluştur
          //         question.addOption(newOption); // Şıkkı soruya ekle (Question Entity'deki yardımcı metot)
          //         questionRepository.save(question); // Soruyu kaydet (şıkları da cascade ile kaydeder)
          //         return newOption;
          //     } else {
          //          throw new RuntimeException("Soru çoktan seçmeli degil, şık eklenemez!");
          //     }
          // }
          // throw new RuntimeException("Soru bulunamadi!"); // Hata yönetimi

         return new Option(); // Şimdilik placeholder
     }

     // Bir Şıkkı Silme
     public void deleteOption(Long optionId) {
         System.out.println("QuizService: Şık silme başlatıldı - Şık ID: " + optionId);
         // Placeholder
         // optionRepository.deleteById(optionId); // Option cascade ve orphanRemoval sayesinde ilgili soru ilişkisinden de kaldırılır
     }

     // Bir Şıkkı Güncelleme
     public Option updateOption(Long optionId, String text, boolean isCorrect) {
         System.out.println("QuizService: Şık güncelleme başlatıldı - Şık ID: " + optionId);
         // Placeholder
         // Optional<Option> optionOptional = optionRepository.findById(optionId);
         // if (optionOptional.isPresent()) {
         //     Option optionToUpdate = optionOptional.get();
         //     optionToUpdate.setText(text);
         //     optionToUpdate.setCorrect(isCorrect);
         //     return optionRepository.save(optionToUpdate);
         // }
         // throw new RuntimeException("Şık bulunamadi!"); // Hata yönetimi

         return new Option(); // Şimdilik placeholder
     }


     // --- Diğer İşlevler ---
     // Sizin Quiz.java ve Teacher.java template'lerindeki istatistik, AI gibi metotlar
     // showQuizStatistics, showQuizAnswers, askAiToAnswerQuiz, CalculateAverage, AskAItoGradeQuiz
     // Bunlar StatisticsService veya AIService gibi ayrı servislere taşınacak.
}
