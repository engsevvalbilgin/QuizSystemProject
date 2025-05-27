package com.example.QuizSystemProject.Service;

import com.example.QuizSystemProject.Model.*;
import com.example.QuizSystemProject.Repository.*;
import com.example.QuizSystemProject.dto.*;
import com.example.QuizSystemProject.exception.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Öğrenci quiz işlemleri için servis sınıfı
 */
@Service
@Transactional(readOnly = true)
public class StudentQuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizSessionRepository quizSessionRepository;
    // AI servisi, açık uçlu soruların değerlendirilmesi için ileride kullanılacak
    @SuppressWarnings("unused")
    private final AIService aiService;
    
    public StudentQuizService(QuizRepository quizRepository, 
                            UserRepository userRepository,
                            QuizSessionRepository quizSessionRepository,
                            AIService aiService) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.quizSessionRepository = quizSessionRepository;
        this.aiService = aiService;
    }
    
    /**
     * Get the current user ID from the security context
     * @return The current user ID
     */
    public int getCurrentUserId() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
            return user.getId();
        } catch (Exception e) {
            throw new RuntimeException("Oturum açmış kullanıcı bulunamadı", e);
        }
    }
    
    /**
     * Get all quiz attempts for a student
     * @param studentId Student ID
     * @return List of QuizAttemptDto
     */
    @Transactional(readOnly = true)
public List<QuizAttemptDto> getStudentQuizAttempts(int studentId) {
    // Find all quiz sessions for the student with all necessary relationships
    List<QuizSession> sessions = quizSessionRepository.findByStudentIdWithDetails(studentId);
    List<QuizAttemptDto> attempts = new ArrayList<>();
    
    for (QuizSession session : sessions) {
        Quiz quiz = session.getQuiz();
        
        QuizAttemptDto attempt = QuizAttemptDto.builder()
            .id(session.getId())
            .quizId(quiz.getId())
            .quizName(quiz.getName())
            .teacherName(quiz.getTeacher() != null ? quiz.getTeacher().getName() : "Unknown")
            .studentId(studentId)
            .startDate(session.getStartTime())
            .completionDate(session.getEndTime())
            .score(session.getScore())
            .passingScore(quiz.getPassingScore())
            .status(session.getEndTime() != null ? "Tamamlandı" : "Devam Ediyor")
            .timeSpentSeconds(session.getTimeSpentSeconds())
            .correctAnswers(session.getCorrectAnswers())
            .totalQuestions(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)
            .totalPoints(quiz.getTotalPoints())
            .earnedPoints(session.getEarnedPoints())
            .build();
            
        attempts.add(attempt);
    }
    
    return attempts;
}
    
    /**
     * Get a specific quiz attempt by ID
     * @param attemptId Quiz session ID
     * @param studentId Student ID
     * @return QuizAttemptDto with detailed information
     */
    @Transactional(readOnly = true)
    public QuizAttemptDto getQuizAttemptById(int attemptId, int studentId) {
        // For backward compatibility, we'll use the detailed version and map to the basic DTO
        DetailedQuizAttemptDto detailedDto = getDetailedQuizAttemptById(attemptId, studentId);
        
        return QuizAttemptDto.builder()
            .id(detailedDto.getId())
            .quizId(detailedDto.getQuizId())
            .quizName(detailedDto.getQuizName())
            .teacherName(detailedDto.getTeacherName())
            .studentId(detailedDto.getStudentId())
            .startDate(detailedDto.getStartDate())
            .completionDate(detailedDto.getCompletionDate())
            .score(detailedDto.getScore())
            .passingScore(detailedDto.getPassingScore())
            .status(detailedDto.getStatus())
            .timeSpentSeconds(detailedDto.getTimeSpentSeconds())
            .correctAnswers(detailedDto.getCorrectAnswers())
            .totalQuestions(detailedDto.getTotalQuestions())
            .totalPoints(detailedDto.getTotalPoints())
            .earnedPoints(detailedDto.getEarnedPoints())
            .build();
    }
    
    /**
     * Get detailed quiz attempt with question results
     * @param attemptId Quiz session ID
     * @param studentId Student ID for authorization
     * @return DetailedQuizAttemptDto with question results
     */
    @Transactional(readOnly = true)
    public DetailedQuizAttemptDto getDetailedQuizAttemptById(int attemptId, int studentId) {
        // Find the quiz session with all necessary relationships
        QuizSession session = quizSessionRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Quiz oturumu bulunamadı: " + attemptId));
            
        // Eagerly fetch the required relationships
        session.getAnswers().size(); // Force load answers
        if (session.getQuiz() != null) {
            session.getQuiz().getQuestions().size(); // Force load questions
            for (Question question : session.getQuiz().getQuestions()) {
                question.getOptions().size(); // Force load options for each question
            }
        }
        
        // Ensure the session belongs to the student
        if (session.getStudent().getId() != studentId) {
            throw new RuntimeException("Bu quiz oturumuna erişim izniniz yok");
        }
        
        Quiz quiz = session.getQuiz();
        
        // Convert answer attempts to question results
        List<DetailedQuizAttemptDto.QuestionResultDto> questionResults = new ArrayList<>();
        
        // Group answer attempts by question
        Map<Question, List<AnswerAttempt>> attemptsByQuestion = session.getAnswers().stream()
            .collect(Collectors.groupingBy(AnswerAttempt::getQuestion));
        
        // Process each question and its answer attempts
        for (Question question : quiz.getQuestions()) {
            List<AnswerAttempt> attempts = attemptsByQuestion.getOrDefault(question, Collections.emptyList());
            processQuestionResults(question, attempts, questionResults);
        }
        
        return DetailedQuizAttemptDto.builder()
            .id(session.getId())
            .quizId(quiz.getId())
            .quizName(quiz.getName())
            .teacherName(quiz.getTeacher() != null ? quiz.getTeacher().getName() : "Unknown")
            .topic(quiz.getTopic())
            .description(quiz.getDescription())
            .studentId(studentId)
            .startDate(session.getStartTime())
            .completionDate(session.getEndTime())
            .score(session.getScore())
            .passingScore(quiz.getPassingScore())
            .status(session.isSubmitted() ? "Tamamlandı" : "Devam Ediyor")
            .timeSpentSeconds(session.getTimeSpentSeconds())
            .correctAnswers(session.getCorrectAnswers())
            .totalQuestions(quiz.getQuestions().size())
            .totalPoints(quiz.getTotalPoints())
            .earnedPoints(session.getEarnedPoints())
            .questionResults(questionResults)
            .build();
    }
    
    /**
     * Process a question and its answer attempts to create a QuestionResultDto
     */
    private void processQuestionResults(Question question, List<AnswerAttempt> attempts, 
            List<DetailedQuizAttemptDto.QuestionResultDto> questionResults) {
        // Get all options for the question and mark correct ones
        List<DetailedQuizAttemptDto.OptionDto> optionDtos = new ArrayList<>();
        List<Integer> correctOptionIds = new ArrayList<>();
        
        for (Option option : question.getOptions()) {
            // Create option DTO
            DetailedQuizAttemptDto.OptionDto optionDto = DetailedQuizAttemptDto.OptionDto.builder()
                .id(option.getId())
                .text(option.getText())
                .isCorrect(option.isCorrect())
                .build();
                
            optionDtos.add(optionDto);
            
            // Track correct option IDs
            if (option.isCorrect()) {
                correctOptionIds.add(option.getId());
            }
        }
        
        // Determine the question type
        // Determine question type based on options
        String questionType = question.getOptions() != null && !question.getOptions().isEmpty() ? 
            "MULTIPLE_CHOICE" : "OPEN_ENDED";
        
        // For multiple choice questions, find the selected options
        List<Integer> selectedOptionIds = new ArrayList<>();
        String submittedTextAnswer = null;
        boolean isCorrect = false;
        double earnedPoints = 0;
        String aiExplanation = null;
        Integer aiScore = null;
        
        if (!attempts.isEmpty()) {
            // We'll take the first attempt (should only be one per question)
            AnswerAttempt attempt = attempts.get(0);
            
            if (questionType.equals("OPEN_ENDED")) {
                submittedTextAnswer = attempt.getSubmittedAnswerText();
                // Açık uçlu sorular için doğru cevabı ekle
                // AI değerlendirme sonuçlarını da al
                aiExplanation = attempt.getAiExplanation();
                aiScore = attempt.getAiScore();
            } else {
                selectedOptionIds = attempt.getSelectedOptions().stream()
                    .map(Option::getId)
                    .collect(Collectors.toList());
            }
            
            isCorrect = attempt.isCorrect();
            earnedPoints = attempt.getEarnedPoints();
        }
        
        // Create and add the question result
        DetailedQuizAttemptDto.QuestionResultDto questionResult = DetailedQuizAttemptDto.QuestionResultDto.builder()
            .questionId(question.getId())
            .questionText(question.getQuestionSentence())
            .questionType(questionType)
            .number(question.getNumber()) // Add question number
            .options(optionDtos)
            .selectedOptionIds(selectedOptionIds)
            .submittedTextAnswer(submittedTextAnswer)
            .isCorrect(isCorrect)
            .earnedPoints(earnedPoints)
            .maxPoints(question.getPoints())
            .correctAnswerText(questionType.equals("OPEN_ENDED") ? question.getCorrectAnswerText() : null)
            .aiExplanation(aiExplanation) // AI açıklaması
            .aiScore(aiScore) // AI puanı
            .build();
            
        questionResults.add(questionResult);
    }

    /**
     * Öğrencinin çözebileceği aktif quizleri getirir
     */
    @Transactional(readOnly = true)
    public List<StudentQuizDto> getAvailableQuizzesForStudent(int studentId) {
        // Öğrenci kontrolü
        if (!userRepository.existsById(studentId)) {
            throw new UserNotFoundException("Öğrenci bulunamadı ID: " + studentId);
        }
        
        System.out.println("Öğrenci ID " + studentId + " için aktif quizler getiriliyor...");
        
        // Aktif quizleri getir (sadece temel bilgileriyle, soru detayları olmadan)
        // Performans iyileştirmesi için findActiveQuizzesBasic kullanılıyor
        List<Quiz> activeQuizzes;
        try {
            activeQuizzes = quizRepository.findActiveQuizzesBasic();
            System.out.println("Toplam " + activeQuizzes.size() + " aktif quiz bulundu.");
        } catch (Exception e) {
            System.err.println("Aktif quizleri getirirken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            // Hata durumunda boş liste dön
            return new ArrayList<>();
        }
        
        // DTO'ları oluştur
        List<StudentQuizDto> result = new ArrayList<>();
        User student = null;
        
        try {
            student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Öğrenci bulunamadı ID: " + studentId));
        } catch (Exception e) {
            System.err.println("Öğrenci bilgisi getirilemedi: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
        
        for (Quiz quiz : activeQuizzes) {
            try {
                // Quiz aktif değilse atla
                if (!quiz.isActive()) {
                    System.out.println("UYARI: Pasif quiz bulundu (ID: " + quiz.getId() + "). Bu bir veri tutarsızlığı olabilir.");
                    continue;
                }
                
                // Quiz soru sayısını bulmak için ayrı bir sorgu yap
                int questionCount = 0;
                try {
                    if (quiz.getQuestions() != null) {
                        questionCount = quiz.getQuestions().size();
                    } else {
                        // Bu noktada sadece quiz ID var, soru bilgisini ayrıca almak gerekebilir
                        Optional<Quiz> quizWithQuestions = quizRepository.findByIdWithQuestionsOnly(quiz.getId());
                        if (quizWithQuestions.isPresent() && quizWithQuestions.get().getQuestions() != null) {
                            questionCount = quizWithQuestions.get().getQuestions().size();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Quiz sorularını sayarken hata: Quiz ID: " + quiz.getId() + ", Hata: " + e.getMessage());
                    // Hatayı yutuyoruz ve devam ediyoruz
                }
                
                // Soru sayısı kontrolü - isteğe bağlı olarak yorum satırına alınabilir
                // if (questionCount == 0) {
                //     System.out.println("Quiz atlandı - Soru yok: " + quiz.getId() + " - " + 
                //                       (quiz.getName() != null ? quiz.getName() : ""));
                //     continue;
                // }
                
                // DTO'yu oluştur
                StudentQuizDto dto = new StudentQuizDto();
                dto.setId(quiz.getId());
                dto.setName(quiz.getName() != null ? quiz.getName() : "");
                dto.setDescription(quiz.getDescription() != null ? quiz.getDescription() : "");
                dto.setTopic(quiz.getTopic() != null ? quiz.getTopic() : "");
                dto.setDurationMinutes(quiz.getDuration());
                dto.setQuestionCount(questionCount);
                dto.setActive(quiz.isActive());
                
                // Daha önce tamamlanmış deneme yapılıp yapılmadığını kontrol et
                boolean attempted = false;
                try {
                    if (student != null && quiz != null) {
                        // Sadece tamamlanmış oturumları kontrol et (endTime null değilse tamamlanmış sayılır)
                        List<QuizSession> completedSessions = quizSessionRepository.findByQuizIdAndStudentId(quiz.getId(), studentId);
                        attempted = completedSessions.stream()
                            .anyMatch(session -> session.getEndTime() != null);
                        
                        System.out.println("Quiz ID: " + quiz.getId() + " - " + 
                                         (attempted ? "TAMAMLANDI" : "TAMAMLANMADI") + 
                                         " (Toplam oturum: " + completedSessions.size() + ")");
                    }
                } catch (Exception e) {
                    System.err.println("Quiz deneme durumu kontrol edilirken hata: Quiz ID: " + 
                                      quiz.getId() + ", Hata: " + e.getMessage());
                    // Hatayı yutuyoruz ve devam ediyoruz
                }
                dto.setAttempted(attempted);
                
                // Öğretmen bilgilerini eklemeye çalış
                if (quiz.getTeacher() != null) {
                    try {
                        StudentQuizDto.TeacherDto teacherDto = new StudentQuizDto.TeacherDto();
                        teacherDto.setId(quiz.getTeacher().getId());
                        teacherDto.setName(quiz.getTeacher().getName());
                        teacherDto.setSurname(quiz.getTeacher().getSurname());
                        dto.setTeacher(teacherDto);
                    } catch (Exception e) {
                        System.err.println("Öğretmen bilgisi eklenirken hata: Quiz ID: " + 
                                         quiz.getId() + ", Hata: " + e.getMessage());
                        // Hatayı yutuyoruz ve devam ediyoruz
                    }
                }
                
                result.add(dto);
                System.out.println("DTO'ya dönüştürülen quiz: " + dto.getId() + " - " + 
                                  dto.getName() + " (Soru Sayısı: " + dto.getQuestionCount() + ")");
                
            } catch (Exception e) {
                System.err.println("Quiz dönüştürülürken hata: Quiz ID: " + 
                                 (quiz != null ? quiz.getId() : "bilinmiyor") + ", Hata: " + e.getMessage());
                e.printStackTrace();
                // Hatalı quizi atla ve devam et
                continue;
            }
        }
        
        System.out.println("Toplam " + result.size() + " quiz öğrenci için uygun bulundu");
        return result;
    }

    /**
     * Get a quiz for a student with the given quiz ID
     * @param quizId The ID of the quiz
     * @param studentId The ID of the student
     * @return The quiz DTO
     */
    @Transactional(readOnly = true)
    public StudentQuizDto getQuizForStudent(int quizId, int studentId) {
        // Check if student exists
        if (!userRepository.existsById(studentId)) {
            throw new UserNotFoundException("Öğrenci bulunamadı ID: " + studentId);
        }

        System.out.println("Öğrenci " + studentId + " için quiz isteniyor. Quiz ID: " + quizId);
        
        // Get the requested quiz with its questions
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
            .orElseThrow(() -> {
                System.out.println("Quiz bulunamadı ID: " + quizId);
                return new QuizNotFoundException("Bu quiz bulunamadı ID: " + quizId);
            });
        
        // Check if quiz is active
        if (!quiz.isActive()) {
            System.out.println("Quiz aktif değil. Quiz ID: " + quizId);
            throw new QuizNotFoundException("Bu quiz aktif değil ID: " + quizId);
        }
        
        // Check if quiz has questions
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            System.out.println("Quiz'de hiç soru yok. Quiz ID: " + quizId);
            throw new QuizNotFoundException("Bu quizde henüz soru bulunmuyor");
        }
        
        System.out.println("Quiz başarıyla getirildi. ID: " + quizId + ", Soru Sayısı: " + quiz.getQuestions().size());
        
        return convertToStudentQuizDto(quiz);
    }
    
    /**
     * Get all quiz attempts for a specific quiz by a specific student
     * @param quizId The ID of the quiz
     * @param studentId The ID of the student
     * @return List of quiz attempts
     */
    @Transactional(readOnly = true)
    public List<QuizAttemptDto> getQuizAttemptsForStudent(int quizId, int studentId) {
        // Öğrenciyi kontrol et
        if (!userRepository.existsById(studentId)) {
            throw new UserNotFoundException("Öğrenci bulunamadı ID: " + studentId);
        }
        
        // Quizi kontrol et
        if (!quizRepository.existsById(quizId)) {
            throw new QuizNotFoundException("Quiz bulunamadı ID: " + quizId);
        }
        
        // Öğrencinin bu quiz için tüm oturumlarını getir
        List<QuizSession> quizSessions = quizSessionRepository.findByQuizIdAndStudentId(quizId, studentId);
        
        // QuizSession'ları QuizAttemptDto'ya dönüştür
        return quizSessions.stream()
                .map(this::convertToQuizAttemptDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert a QuizSession to a QuizAttemptDto
     */
    private QuizAttemptDto convertToQuizAttemptDto(QuizSession quizSession) {
        if (quizSession == null) {
            return null;
        }
        
        // Get the quiz from the session
        Quiz quiz = quizSession.getQuiz();
        int totalQuestions = (quiz != null && quiz.getQuestions() != null) ? 
                            quiz.getQuestions().size() : 0;
                            
        // Calculate passing score (default to 70% of total questions if not set)
        int passingScore = (quiz != null && quiz.getPassingScore() > 0) ? 
                          quiz.getPassingScore() : 
                          (int) Math.ceil(totalQuestions * 0.7);
        
        // Build teacher name
        String teacherName = "Bilinmiyor";
        if (quiz != null && quiz.getTeacher() != null) {
            User teacher = quiz.getTeacher();
            teacherName = (teacher.getName() != null ? teacher.getName() + " " : "") +
                        (teacher.getSurname() != null ? teacher.getSurname() : "").trim();
        }
        
        // Determine status based on completion
        String status = quizSession.isCompleted() ? "COMPLETED" : "IN_PROGRESS";
        
        // Build and return the DTO
        return QuizAttemptDto.builder()
                .id(quizSession.getId())
                .quizId(quiz != null ? quiz.getId() : 0)
                .quizName(quiz != null ? quiz.getName() : "Bilinmeyen Quiz")
                .teacherName(teacherName)
                .studentId(quizSession.getStudent() != null ? quizSession.getStudent().getId() : 0)
                .startDate(quizSession.getStartTime())
                .completionDate(quizSession.getEndTime())
                .score(quizSession.getScore())
                .passingScore(passingScore)
                .status(status)
                .timeSpentSeconds(quizSession.getTimeSpentSeconds() != null ? 
                                 quizSession.getTimeSpentSeconds() : 0)
                .correctAnswers(quizSession.getCorrectAnswers())
                .totalQuestions(totalQuestions)
                .totalPoints(quiz != null ? quiz.getQuestions().stream().mapToInt(Question::getPoints).sum() : 0) // Toplam mümkün olan puanı hesapla
                .earnedPoints(quizSession.getEarnedPoints())
                .build();
    }

    /**
     * Öğrenciye ait belirli bir quiz'i sorularıyla birlikte getirir
     * 
     * @param studentId Öğrenci ID'si
     * @param quizId Quiz ID'si
     * @return Quiz ve soruları içeren DTO
     */
    @Transactional(readOnly = true)
    public QuizWithQuestionsDto getQuizWithQuestions(int studentId, int quizId) {
        // Check if student exists
        if (!userRepository.existsById(studentId)) {
            throw new UserNotFoundException("Öğrenci bulunamadı ID: " + studentId);
        }
        
        System.out.println("Öğrenci " + studentId + " için quiz getiriliyor. Quiz ID: " + quizId);
        
        // Get all active quizzes with their questions and options
        List<Quiz> activeQuizzes = quizRepository.findActiveQuizzesWithQuestionsAndOptions();
        
        // Find the requested quiz
        Quiz quiz = activeQuizzes.stream()
                .filter(q -> q.getId() == quizId)
                .findFirst()
                .orElseThrow(() -> {
                    System.out.println("Aktif quiz bulunamadı ID: " + quizId);
                    return new QuizNotFoundException("Bu quiz aktif değil veya bulunamadı ID: " + quizId);
                });
        
        // Check if quiz has questions
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            System.out.println("Quiz'de hiç soru yok. Quiz ID: " + quizId);
            throw new QuizNotFoundException("Bu quizde henüz soru bulunmuyor");
        }
        
        // Soruların seçeneklerini kontrol et ve gerekirse yükle
        for (Question question : quiz.getQuestions()) {
            if (question.getOptions() == null) {
                question.setOptions(new ArrayList<>());
            }
        }
        
        System.out.println("Quiz başarıyla getirildi. ID: " + quizId + ", Soru Sayısı: " + quiz.getQuestions().size());
        if (!quiz.getQuestions().isEmpty()) {
            Question firstQuestion = quiz.getQuestions().get(0);
            System.out.println("İlk soru: " + firstQuestion.getQuestionSentence());
            System.out.println("İlk sorunun seçenek sayısı: " + 
                (firstQuestion.getOptions() != null ? firstQuestion.getOptions().size() : 0));
        }
        
        try {
            // DTO'ya dönüştür ve döndür
            QuizWithQuestionsDto dto = convertToQuizWithQuestionsDto(quiz);
            System.out.println("Quiz DTO'ya başarıyla dönüştürüldü. Toplam soru sayısı: " + 
                (dto.getQuestions() != null ? dto.getQuestions().size() : 0));
            return dto;
        } catch (Exception e) {
            System.err.println("Quiz DTO'ya dönüştürülürken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Quiz yüklenirken bir hata oluştu", e);
        }
    }
    
    /**
     * Quiz'i StudentQuizDto'ya dönüştürür
     */
    private StudentQuizDto convertToStudentQuizDto(Quiz quiz) {
        StudentQuizDto dto = new StudentQuizDto();
        dto.setId(quiz.getId());
        dto.setName(quiz.getName());
        dto.setDescription(quiz.getDescription());
        dto.setDurationMinutes(quiz.getDuration());
        dto.setActive(quiz.isActive()); // Aktiflik durumunu ayarla
        dto.setTopic(quiz.getTopic()); // Konu bilgisini ayarla
        // Eğer Quiz sınıfında created_at alanı yoksa şimdilik null bırakıyoruz
        // dto.setCreatedAt(quiz.getCreatedAt());
        dto.setQuestionCount(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0);

        // Öğretmen bilgilerini doldur
        if (quiz.getTeacher() != null) {
            StudentQuizDto.TeacherDto teacherDto = new StudentQuizDto.TeacherDto();
            teacherDto.setId(quiz.getTeacher().getId());
            teacherDto.setName(quiz.getTeacher().getName());
            teacherDto.setSurname(quiz.getTeacher().getSurname());
            dto.setTeacher(teacherDto);
        }

        return dto;
    }
    
  
    
    /**
     * Quiz nesnesini QuizWithQuestionsDto'ya dönüştürür
     * @param quiz Dönüştürülecek quiz nesnesi
     * @return Dönüştürülmüş QuizWithQuestionsDto
     */
    @Transactional(readOnly = true)
    private QuizWithQuestionsDto convertToQuizWithQuestionsDto(Quiz quiz) {
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz nesnesi boş olamaz");
        }

        System.out.println("Quiz DTO'ya dönüştürülüyor. Quiz ID: " + quiz.getId());
        
        try {
            QuizWithQuestionsDto dto = new QuizWithQuestionsDto();
            dto.setId(quiz.getId());
            dto.setName(quiz.getName() != null ? quiz.getName() : "");
            dto.setDescription(quiz.getDescription() != null ? quiz.getDescription() : "");
            dto.setTopic(quiz.getTopic() != null ? quiz.getTopic() : "");
            dto.setDurationMinutes(quiz.getDuration());
            dto.setActive(quiz.isActive()); // Explicitly set the active status from the quiz
            
            // Quiz'in sorularını al (sorular zaten yüklü olmalı)
            List<Question> questions = quiz.getQuestions();
            if (questions == null || questions.isEmpty()) {
                System.out.println("Uyarı: Quiz'de hiç soru bulunamadı. Quiz ID: " + quiz.getId());
                dto.setQuestions(new ArrayList<>());
                return dto;
            }
            
            // Soruları numara sırasına göre sırala
            questions.sort((q1, q2) -> Integer.compare(q1.getNumber(), q2.getNumber()));
            System.out.println("Sorular numara sırasına göre sıralandı.");
            
            System.out.println("Toplam " + questions.size() + " soru işleniyor...");
            
            // Soruları DTO'ya dönüştür
            List<QuestionDto> questionDtos = new ArrayList<>();
            
            for (Question question : questions) {
                try {
                    if (question == null) {
                        System.out.println("Uyarı: Null soru atlandı");
                        continue;
                    }
                    
                    QuestionDto questionDto = new QuestionDto();
                    questionDto.setId(question.getId());
                    questionDto.setText(question.getQuestionSentence() != null ? 
                            question.getQuestionSentence() : "");
                    
                    // Soru tipini ayarla (1: Çoktan seçmeli, 2: Açık uçlu)
                    int questionTypeId = (question.getType() != null) ? question.getType().getId() : 1;
                    questionDto.setQuestionTypeId(questionTypeId);
                    
                    // Soru puanını ayarla
                    questionDto.setPoints(question.getPoints());
                    
                    // Seçenekleri işle
                    List<OptionDto> optionDtos = new ArrayList<>();
                    if (questionTypeId == 1 && question.getOptions() != null) { // Sadece çoktan seçmeli sorular için
                        for (Option option : question.getOptions()) {
                            if (option != null) {
                                OptionDto optionDto = new OptionDto();
                                optionDto.setId(option.getId());
                                optionDto.setText(option.getText() != null ? option.getText() : "");
                                optionDtos.add(optionDto);
                            }
                        }
                    }
                    questionDto.setAnswers(optionDtos);
                    
                    questionDtos.add(questionDto);
                    
                } catch (Exception e) {
                    System.err.println("Soru dönüştürülürken hata oluştu. Soru ID: " + 
                            (question != null ? question.getId() : "null") + 
                            ", Hata: " + e.getMessage());
                    // Hatalı soruyu atla ve devam et
                }
            }
            
            dto.setQuestions(questionDtos);
            System.out.println("Başarıyla " + questionDtos.size() + " soru dönüştürüldü.");
            return dto;
            
        } catch (Exception e) {
            System.err.println("Quiz DTO'ya dönüştürülürken beklenmeyen hata: " + e.getMessage());
            e.printStackTrace();
            throw e; // Hatanın üst katmana iletilmesi için fırlat
        }
    }
    
    /**
     * Quiz başlatma işlemini gerçekleştirir
     * @param quizId Başlatılacak quiz ID'si
     * @param studentId Öğrenci ID'si
     * @return Quiz detaylarını içeren DTO
     */
    @Transactional
    public QuizSessionStartResponse startQuizSession(int quizId, int studentId) {
        System.out.println("Quiz oturumu başlatılıyor. Quiz ID: " + quizId + ", Öğrenci ID: " + studentId);
        
        try {
            // Öğrenci varlığını kontrol et
            User student = null;
            try {
                student = userRepository.findById(studentId)
                    .orElseThrow(() -> new UserNotFoundException("Öğrenci bulunamadı ID: " + studentId));
                System.out.println("Öğrenci bulundu: " + student.getUsername());
            } catch (Exception e) {
                System.err.println("Öğrenci bilgisi alınırken hata: " + e.getMessage());
                e.printStackTrace();
                throw new UserNotFoundException("Öğrenci bilgisi alınamadı ID: " + studentId + ", Hata: " + e.getMessage());
            }
                
            // Quiz'i veritabanından getir - optimizasyon için findByIdWithQuestionsOnly kullanıyoruz
            Quiz quiz;
            try {
                System.out.println("Quiz veritabanından getiriliyor, ID: " + quizId);
                // Doğrudan quiz ID'si ile sorgu yapalım
                Optional<Quiz> quizOpt = quizRepository.findByIdWithQuestionsOnly(quizId);
                
                if (!quizOpt.isPresent()) {
                    System.out.println("Quiz bulunamadı ID: " + quizId);
                    throw new QuizNotFoundException("Quiz bulunamadı ID: " + quizId);
                }
                
                quiz = quizOpt.get();
                
                // Quiz'in aktif olup olmadığını kontrol et
                if (!quiz.isActive()) {
                    System.out.println("Quiz aktif değil. Quiz ID: " + quizId);
                    throw new QuizNotFoundException("Bu quiz aktif değil ID: " + quizId);
                }
            } catch (Exception e) {
                System.err.println("Quiz bilgisi alınırken hata: " + e.getMessage());
                e.printStackTrace();
                throw new QuizNotFoundException("Quiz bilgisi alınamadı ID: " + quizId + ", Hata: " + e.getMessage());
            }
                
            System.out.println("Quiz başarıyla getirildi. ID: " + quizId + ", Soru Sayısı: " + 
                (quiz.getQuestions() != null ? quiz.getQuestions().size() : 0));
            
            // Sorular kontrolü
            if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
                System.out.println("Quiz'de hiç soru bulunamadı. Quiz ID: " + quizId);
                throw new QuizNotFoundException("Bu quizde hiç soru bulunmuyor");
            }

            // Önce bu öğrenci için bu quiz ile ilgili tüm oturumları al
            List<QuizSession> allSessions = quizSessionRepository.findByQuizIdAndStudentId(quizId, studentId);
            
            // Eğer tamamlanmış bir oturum varsa hata döndür
            boolean hasCompletedSession = allSessions.stream()
                .anyMatch(session -> session.getEndTime() != null);
            
            if (hasCompletedSession) {
                System.out.println("Bu quiz zaten tamamlanmış. Quiz ID: " + quizId + ", Öğrenci ID: " + studentId);
                throw new RuntimeException("Bu quiz zaten tamamlanmış. Yeniden başlatılamaz.");
            }
            
            // Aktif bir oturum var mı kontrol et
            Optional<QuizSession> existingActiveSession = allSessions.stream()
                .filter(session -> session.getEndTime() == null)
                .findFirst();
                
            QuizSession session;
            
            if (existingActiveSession.isPresent()) {
                // Mevcut aktif oturumu kullan
                session = existingActiveSession.get();
                System.out.println("Mevcut aktif oturum kullanılıyor. Session ID: " + session.getId());
            } else {
                // Yeni bir oturum oluşturmadan önce tekrar kontrol et (race condition için)
                synchronized(this) {
                    // Tekrar kontrol et (double-checked locking pattern)
                    Optional<QuizSession> recheckSession = quizSessionRepository.findActiveSessionByStudentAndQuiz(studentId, quizId);
                    if (recheckSession.isPresent()) {
                        session = recheckSession.get();
                        System.out.println("Eşzamanlı istek tespit edildi, mevcut oturum kullanılıyor. Session ID: " + session.getId());
                    } else {
                        // Yeni bir quiz oturumu oluştur
                        session = new QuizSession(student, quiz);
                        session.setStartTime(LocalDateTime.now());
                        session = quizSessionRepository.save(session);
                        System.out.println("Yeni quiz oturumu oluşturuldu. Session ID: " + session.getId());
                    }
                }
            }
            
            // Debug amaçlı bilgileri yazdır
            System.out.println("Quiz bilgileri:");
            System.out.println("ID: " + quiz.getId());
            System.out.println("Name: " + (quiz.getName() != null ? quiz.getName() : "<isim yok>"));
            System.out.println("Questions size: " + quiz.getQuestions().size());
            
            // Her soru için seçenekleri kontrol et
            for (int i = 0; i < quiz.getQuestions().size(); i++) {
                Question q = quiz.getQuestions().get(i);
                System.out.println("  Soru " + (i+1) + ": " + 
                    (q.getQuestionSentence() != null ? q.getQuestionSentence() : "<soru metni yok>"));
                
                // Null kontrolü yap ve seçenekleri yükle veya boş liste ata
                if (q.getOptions() == null) {
                    q.setOptions(new ArrayList<>());
                }
                System.out.println("    Seçenek sayısı: " + q.getOptions().size());
            }
            
            System.out.println("Quiz oturumu başarıyla başlatıldı. Quiz ID: " + quizId);
            
            // QuizWithQuestionsDto'ya dönüştür
            QuizWithQuestionsDto quizWithQuestions = convertToQuizWithQuestionsDto(quiz);
            System.out.println("Dönüştürülen DTO'da soru sayısı: " + 
                (quizWithQuestions.getQuestions() != null ? quizWithQuestions.getQuestions().size() : 0));
            
            // Oturum bilgisi ve quiz verilerini içeren yanıtı oluştur
            
            QuizSessionStartResponse response = new QuizSessionStartResponse();
            response.setSessionId(session.getId());
            response.setQuiz(quizWithQuestions);
            return response;
            
        } catch (Exception e) {
            System.err.println("Quiz oturumu başlatılırken hata oluştu - Quiz ID: " + quizId);
            e.printStackTrace();
            throw new RuntimeException("Quiz oturumu başlatılamadı: " + e.getMessage(), e);
        }
    }
    
    /**
     * Quiz oturumunu tamamlar ve sonuçları hesaplar
     */
    @Transactional
    public QuizResultDto completeQuizSession(QuizCompletionRequestDto completionRequest, int studentId) {
        try {
            int quizId = completionRequest.getQuizId();
            
            // Quizi veritabanından al
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz bulunamadı, ID: " + quizId));
            
            // Öğrencinin var olup olmadığını kontrol et
            userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı, ID: " + studentId));
            
            System.out.println("Quiz ve öğrenci doğrulandı");
            
            // Aktif quiz oturumunu bul
            List<QuizSession> sessions = quizSessionRepository.findByQuizIdAndStudentId(quizId, studentId);
            if (sessions.isEmpty()) {
                throw new RuntimeException("Bu quiz için aktif oturum bulunamadı. Lütfen önce quizi başlatın.");
            }
            
            // En son oturumu al (Muhtemelen sadece 1 tane olacak)
            QuizSession session = sessions.get(sessions.size() - 1);
            System.out.println("Quiz oturumu bulundu. Oturum ID: " + session.getId());
            
            // Oturum zaten tamamlanmışsa hata fırlat
            if (session.getEndTime() != null) {
                throw new RuntimeException("Bu quiz oturumu zaten tamamlanmış.");
            }
            
            // Oturumu tamamla
            session.setEndTime(LocalDateTime.now());
            
            // Cevapları işle ve puanlamayı yap
            int totalQuestions = quiz.getQuestions().size();
            int correctAnswers = 0;
            List<QuizResultDto.QuestionResultDto> questionResults = new ArrayList<>();
            
            System.out.println("Toplam " + totalQuestions + " soru bulundu, işlenen cevap sayısı: " + completionRequest.getAnswers().size());
            
            // Öğrencinin cevaplarını doğrula ve puanla
            for (QuizCompletionRequestDto.StudentAnswerDto answer : completionRequest.getAnswers()) {
                System.out.println("Cevap işleniyor - Soru ID: " + answer.getQuestionId() + 
                                 ", Seçilen Cevap ID: " + answer.getAnswerId() + 
                                 ", Metin Cevap: " + answer.getTextAnswer());
                
                // Soruyu bul
                Question question = quiz.getQuestions().stream()
                        .filter(q -> q.getId() == answer.getQuestionId())
                        .findFirst()
                        .orElseThrow(() -> {
                            String errorMsg = "Soru bulunamadı, ID: " + answer.getQuestionId() + 
                                          ". Mevcut soru ID'leri: " + 
                                          quiz.getQuestions().stream().map(q -> String.valueOf(q.getId())).collect(Collectors.joining(", "));
                            System.err.println(errorMsg);
                            return new RuntimeException(errorMsg);
                        });
                
                System.out.println("Soru bulundu: " + question.getQuestionSentence());
                
                // Soru tipine göre işlem yap
                boolean isCorrect = false;
                AnswerAttempt answerAttempt = new AnswerAttempt();
                answerAttempt.setQuizSession(session);
                answerAttempt.setQuestion(question);
                
                // QuestionResultDto hazırla
                QuizResultDto.QuestionResultDto questionResult = new QuizResultDto.QuestionResultDto();
                questionResult.setQuestionId(question.getId());
                questionResult.setQuestionText(question.getQuestionSentence());
                
                // Soru tipini logla
                System.out.println("Soru tipi: " + (question.getType() != null ? 
                    (question.getType().getId() == 1 ? "Çoktan Seçmeli" : "Açık Uçlu") : "Bilinmeyen"));
                
                // Soru tipine göre farklı işlemler yap
                if (question.getType() != null && question.getType().getId() == 1) {
                    // Çoktan seçmeli soru
                    System.out.println("Çoktan seçmeli soru işleniyor. Seçilen cevap ID: " + answer.getAnswerId());
                    
                    // Seçilen cevabı bul (eğer seçilmişse)
                    Option selectedOption = null;
                    if (answer.getAnswerId() != 0) { // 0, cevap seçilmediği anlamına gelir
                        selectedOption = question.getOptions().stream()
                                .filter(o -> o.getId() == answer.getAnswerId())
                                .findFirst()
                                .orElseThrow(() -> {
                                    String errorMsg = "Seçenek bulunamadı, ID: " + answer.getAnswerId() + 
                                                  ". Mevcut seçenekler: " + 
                                                  question.getOptions().stream()
                                                         .map(o -> o.getId() + "(" + o.getText() + ")")
                                                         .collect(Collectors.joining(", "));
                                    System.err.println(errorMsg);
                                    return new RuntimeException(errorMsg);
                                });
                        System.out.println("Seçilen seçenek: " + selectedOption.getText());
                    } else {
                        System.out.println("Bu soru için cevap seçilmemiş.");
                    }
                    
                    // Doğru cevabı bul
                    List<Option> correctOptions = question.getOptions().stream()
                            .filter(Option::isCorrect)
                            .collect(Collectors.toList());
                    
                    Option correctOption = correctOptions.isEmpty() ? null : correctOptions.get(0);
                    System.out.println("Doğru cevap: " + (correctOption != null ? 
                        (correctOption.getId() + " - " + correctOption.getText()) : "Tanımlı değil"));
                    
                    // Cevap seçilmişse işlem yap
                    if (selectedOption != null) {
                        // Cevabın doğru olup olmadığını kontrol et
                        if (correctOption != null && selectedOption.getId() == correctOption.getId()) {
                            isCorrect = true;
                            correctAnswers++;
                            answerAttempt.setEarnedPoints(question.getPoints()); // Correct MCQ earns full points
                        } else {
                            answerAttempt.setEarnedPoints(0); // Incorrect MCQ earns 0 points
                        }
                        
                        // Seçilen cevabı ekle
                        java.util.Set<Option> selectedOptions = new java.util.HashSet<>();
                        selectedOptions.add(selectedOption);
                        answerAttempt.setSelectedOptions(selectedOptions);
                        
                        // DTO'ya cevap bilgilerini ekle
                        questionResult.setSelectedAnswerId(selectedOption.getId());
                    } else {
                        // Cevap seçilmemişse boş bir set ata ve 0 puan ver
                        answerAttempt.setSelectedOptions(new java.util.HashSet<>());
                        answerAttempt.setEarnedPoints(0); // Unanswered MCQ earns 0 points
                        questionResult.setSelectedAnswerId(0); // 0, cevap seçilmediği anlamına gelir
                    }
                    
                    // Doğru cevap ID'sini ayarla
                    questionResult.setCorrectAnswerId(correctOption != null ? correctOption.getId() : 0);
                } else {
                    // Açık uçlu soru
                    // Text cevabını al ve null kontrolü yap
                    String textAnswer = answer.getTextAnswer();
                    if (textAnswer == null) {
                        textAnswer = ""; // Null ise boş string olarak ayarla
                        System.out.println("Açık uçlu soru için cevap girilmemiş.");
                    } else {
                        System.out.println("Açık uçlu soru işleniyor. Cevap: " + textAnswer);
                    }
                    
                    // Metin cevabını kaydet (boş bile olsa kaydet)
                    answerAttempt.setSubmittedAnswerText(textAnswer);
                    
                    // Açık uçlu sorular için AI servisini çağır
                    OpenEndedEvaluationResultDto evaluationResult = aiService.evaluateOpenEndedAnswer(
                        question.getQuestionSentence(), 
                        textAnswer, 
                        question.getCorrectAnswerText(),
                        question.getPoints()
                    );
                    
                    // Log the results from AI Service
                    System.out.println("AI Service returned - Points: " + evaluationResult.getEarnedPoints() + "/" + question.getPoints() + ", Explanation: " + evaluationResult.getExplanation());
                    
                    // For open-ended questions, isCorrect is based on whether they earned points
                    isCorrect = evaluationResult.getEarnedPoints() > 0;
                    
                    // Set earned points from AI evaluation
                    answerAttempt.setEarnedPoints(evaluationResult.getEarnedPoints());
                    
                    // AI değerlendirme sonuçlarını veritabanına kaydet
                    answerAttempt.setAiExplanation(evaluationResult.getExplanation());
                    answerAttempt.setAiScore(evaluationResult.getEarnedPoints());
                    
                    // DTO'yu açık uçlu soru için güncelle
                    questionResult.setTextAnswer(textAnswer);
                    questionResult.setRequiresManualGrading(true);
                    questionResult.setEarnedPoints(evaluationResult.getEarnedPoints()); 
                    questionResult.setAiExplanation(evaluationResult.getExplanation());
                    questionResult.setAiScore(evaluationResult.getEarnedPoints());
                    
                    // Add to correct answers count if points were earned
                    if (evaluationResult.getEarnedPoints() > 0) {
                        correctAnswers++;
                    }
                    
                    System.out.println("Açık uçlu soru işlendi - Cevap kaydedildi ve manuel değerlendirme gerekiyor");
                }
                
                // Doğruluk durumunu kaydet
                answerAttempt.setCorrect(isCorrect);
                questionResult.setCorrect(isCorrect);
                
                // Oturuma cevap girişimini ekle
                session.addAnswerAttempt(answerAttempt);
                questionResults.add(questionResult);
            }
            
            // Toplam puanı hesapla
            int totalPossiblePoints = quiz.getQuestions().stream()
                    .mapToInt(Question::getPoints)
                    .sum();
            
            int earnedPoints = 0;
            
            // Her soru için kazanılan puanları topla (AnswerAttempt'teki earnedPoints'i kullan)
            for (AnswerAttempt attempt : session.getAnswers()) {
                earnedPoints += attempt.getEarnedPoints();
                System.out.println("Soru için kazanılan puan: " + attempt.getEarnedPoints() + ". Toplam puan: " + earnedPoints);
            }
            
            // Yüzdelik skoru hesapla (0-100 arası)
            double scorePercentage = totalPossiblePoints > 0 ? 
                    (earnedPoints * 100.0) / totalPossiblePoints : 0;
                    
            // Puanları kaydet
            session.setScore((int) Math.round(scorePercentage)); // Yüzdeyi tam sayıya yuvarla
            session.setEarnedPoints(earnedPoints);
            session.setCorrectAnswers(correctAnswers);
            
            System.out.println(String.format("Quiz puanı hesaplandı: %.2f/100 (Puan: %d/%d, Doğru cevap: %d/%d)", 
                    scorePercentage, earnedPoints, totalPossiblePoints, correctAnswers, totalQuestions));
            
            try {
                // Oturumu veritabanına kaydet
                quizSessionRepository.save(session);
                
                // Sonuç DTO'sunu oluştur
                QuizResultDto result = new QuizResultDto();
                result.setAttemptId(session.getId());
                result.setQuizId(quiz.getId());
                result.setQuizName(quiz.getName());
                result.setScore((int) Math.round(scorePercentage));
                result.setTotalQuestions(totalQuestions);
                result.setCorrectAnswers(correctAnswers);
                result.setTimeSpent(completionRequest.getTimeSpent());
                result.setCompletionDate(session.getEndTime().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                result.setTotalPoints(totalPossiblePoints);
                result.setEarnedPoints(earnedPoints);
                result.setPassingScore(quiz.getPassingScore());
                result.setPassed(earnedPoints >= quiz.getPassingScore());
                
                // Her soru için puan bilgilerini ayarla
                for (QuizResultDto.QuestionResultDto qr : questionResults) {
                    qr.setPoints(quiz.getQuestions().stream()
                            .filter(q -> q.getId() == qr.getQuestionId())
                            .findFirst()
                            .map(Question::getPoints)
                            .orElse(0));
                    // Açık uçlu sorular için AI'dan gelen puanı, çoktan seçmeli sorular için isCorrect durumuna göre puanı kullan
                    if (qr.getAiExplanation() != null && !qr.getAiExplanation().isEmpty()) { // Açık uçlu soru ise
                        // If we have an AI score, use it (it should already be set correctly)
                        if (qr.getAiScore() != null && qr.getAiScore() > 0) {
                            qr.setEarnedPoints(qr.getAiScore());
                            System.out.println("Setting earned points for open-ended question to AI score: " + qr.getAiScore());
                        } else {
                            // Fallback: use the existing earned points (should be set from answerAttempt.getEarnedPoints())
                            System.out.println("Using existing earnedPoints for open-ended question: " + qr.getEarnedPoints());
                        }
                    } else { // Çoktan seçmeli soru ise
                        qr.setEarnedPoints(qr.isCorrect() ? qr.getPoints() : 0);
                        System.out.println("Setting earned points for multiple choice question: " + 
                            (qr.isCorrect() ? qr.getPoints() : 0) + "/" + qr.getPoints());
                    }
                }
                
                result.setQuestionResults(questionResults);
                
                System.out.println("Quiz sonuç DTO'su oluşturuldu. Toplam soru sonucu: " + questionResults.size());
                
                return result;
            } catch (Exception e) {
                System.err.println("Quiz oturumu kaydedilirken hata oluştu: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            
        } catch (Exception e) {
            System.err.println("Quiz tamamlanırken hata oluştu - Quiz ID: " + completionRequest.getQuizId());
            e.printStackTrace();
            throw new RuntimeException("Quiz tamamlanamadı: " + e.getMessage(), e);
        }
    }
}
