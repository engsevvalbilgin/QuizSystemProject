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



@Service
@Transactional(readOnly = true)
public class StudentQuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizSessionRepository quizSessionRepository;
  
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
    
    
    @Transactional(readOnly = true)
public List<QuizAttemptDto> getStudentQuizAttempts(int studentId) {
    
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
    
    
    @Transactional(readOnly = true)
    public QuizAttemptDto getQuizAttemptById(int attemptId, int studentId) {
        
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
    
    
    @Transactional(readOnly = true)
    public DetailedQuizAttemptDto getDetailedQuizAttemptById(int attemptId, int studentId) {
        
        QuizSession session = quizSessionRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Quiz oturumu bulunamadı: " + attemptId));
            
        
        session.getAnswers().size(); 
        if (session.getQuiz() != null) {
            session.getQuiz().getQuestions().size(); 
            for (Question question : session.getQuiz().getQuestions()) {
                question.getOptions().size(); 
            }
        }
        
        if (session.getStudent().getId() != studentId) {
            throw new RuntimeException("Bu quiz oturumuna erişim izniniz yok");
        }
        
        Quiz quiz = session.getQuiz();
        
       
        List<DetailedQuizAttemptDto.QuestionResultDto> questionResults = new ArrayList<>();
        
        
        Map<Question, List<AnswerAttempt>> attemptsByQuestion = session.getAnswers().stream()
            .collect(Collectors.groupingBy(AnswerAttempt::getQuestion));
        
        
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
    
  
    private void processQuestionResults(Question question, List<AnswerAttempt> attempts, 
            List<DetailedQuizAttemptDto.QuestionResultDto> questionResults) {
        
        List<DetailedQuizAttemptDto.OptionDto> optionDtos = new ArrayList<>();
        List<Integer> correctOptionIds = new ArrayList<>();
        
        for (Option option : question.getOptions()) {
            DetailedQuizAttemptDto.OptionDto optionDto = DetailedQuizAttemptDto.OptionDto.builder()
                .id(option.getId())
                .text(option.getText())
                .isCorrect(option.isCorrect())
                .build();
                
            optionDtos.add(optionDto);
            
            if (option.isCorrect()) {
                correctOptionIds.add(option.getId());
            }
        }
        
       
    
        String questionType = question.getOptions() != null && !question.getOptions().isEmpty() ? 
            "MULTIPLE_CHOICE" : "OPEN_ENDED";
        
        
        List<Integer> selectedOptionIds = new ArrayList<>();
        String submittedTextAnswer = null;
        boolean isCorrect = false;
        double earnedPoints = 0;
        String aiExplanation = null;
        Integer aiScore = null;
        
        if (!attempts.isEmpty()) {
            AnswerAttempt attempt = attempts.get(0);
            
            if (questionType.equals("OPEN_ENDED")) {
                submittedTextAnswer = attempt.getSubmittedAnswerText();
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
        
        
        DetailedQuizAttemptDto.QuestionResultDto questionResult = DetailedQuizAttemptDto.QuestionResultDto.builder()
            .questionId(question.getId())
            .questionText(question.getQuestionSentence())
            .questionType(questionType)
            .number(question.getNumber()) 
            .options(optionDtos)
            .selectedOptionIds(selectedOptionIds)
            .submittedTextAnswer(submittedTextAnswer)
            .isCorrect(isCorrect)
            .earnedPoints(earnedPoints)
            .maxPoints(question.getPoints())
            .correctAnswerText(questionType.equals("OPEN_ENDED") ? question.getCorrectAnswerText() : null)
            .aiExplanation(aiExplanation) 
            .aiScore(aiScore) 
            .build();
            
        questionResults.add(questionResult);
    }

    
    @Transactional(readOnly = true)
    public List<StudentQuizDto> getAvailableQuizzesForStudent(int studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new UserNotFoundException("Öğrenci bulunamadı ID: " + studentId);
        }
        
        System.out.println("Öğrenci ID " + studentId + " için aktif quizler getiriliyor...");
        
        List<Quiz> activeQuizzes;
        try {
            activeQuizzes = quizRepository.findActiveQuizzesBasic();
            System.out.println("Toplam " + activeQuizzes.size() + " aktif quiz bulundu.");
        } catch (Exception e) {
            System.err.println("Aktif quizleri getirirken hata oluştu: " + e.getMessage());
            e.printStackTrace();
          
            return new ArrayList<>();
        }
        
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
                if (!quiz.isActive()) {
                    System.out.println("UYARI: Pasif quiz bulundu (ID: " + quiz.getId() + "). Bu bir veri tutarsızlığı olabilir.");
                    continue;
                }
                
                int questionCount = 0;
                try {
                    if (quiz.getQuestions() != null) {
                        questionCount = quiz.getQuestions().size();
                    } else {
                        
                        Optional<Quiz> quizWithQuestions = quizRepository.findByIdWithQuestionsOnly(quiz.getId());
                        if (quizWithQuestions.isPresent() && quizWithQuestions.get().getQuestions() != null) {
                            questionCount = quizWithQuestions.get().getQuestions().size();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Quiz sorularını sayarken hata: Quiz ID: " + quiz.getId() + ", Hata: " + e.getMessage());
                }
                
                
              
                StudentQuizDto dto = new StudentQuizDto();
                dto.setId(quiz.getId());
                dto.setName(quiz.getName() != null ? quiz.getName() : "");
                dto.setDescription(quiz.getDescription() != null ? quiz.getDescription() : "");
                dto.setTopic(quiz.getTopic() != null ? quiz.getTopic() : "");
                dto.setDurationMinutes(quiz.getDuration());
                dto.setQuestionCount(questionCount);
                dto.setActive(quiz.isActive());
                
                
                boolean attempted = false;
                try {
                    if (student != null && quiz != null) {
                        
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
                    
                }
                dto.setAttempted(attempted);
                
                
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
                        
                    }
                }
                
                result.add(dto);
                System.out.println("DTO'ya dönüştürülen quiz: " + dto.getId() + " - " + 
                                  dto.getName() + " (Soru Sayısı: " + dto.getQuestionCount() + ")");
                
            } catch (Exception e) {
                System.err.println("Quiz dönüştürülürken hata: Quiz ID: " + 
                                 (quiz != null ? quiz.getId() : "bilinmiyor") + ", Hata: " + e.getMessage());
                e.printStackTrace();
                
                continue;
            }
        }
        
        System.out.println("Toplam " + result.size() + " quiz öğrenci için uygun bulundu");
        return result;
    }

   
    @Transactional(readOnly = true)
    public StudentQuizDto getQuizForStudent(int quizId, int studentId) {
        
        if (!userRepository.existsById(studentId)) {
            throw new UserNotFoundException("Öğrenci bulunamadı ID: " + studentId);
        }

        System.out.println("Öğrenci " + studentId + " için quiz isteniyor. Quiz ID: " + quizId);
        
        
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
            .orElseThrow(() -> {
                System.out.println("Quiz bulunamadı ID: " + quizId);
                return new QuizNotFoundException("Bu quiz bulunamadı ID: " + quizId);
            });
        
        
        if (!quiz.isActive()) {
            System.out.println("Quiz aktif değil. Quiz ID: " + quizId);
            throw new QuizNotFoundException("Bu quiz aktif değil ID: " + quizId);
        }
        
        
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            System.out.println("Quiz'de hiç soru yok. Quiz ID: " + quizId);
            throw new QuizNotFoundException("Bu quizde henüz soru bulunmuyor");
        }
        
        System.out.println("Quiz başarıyla getirildi. ID: " + quizId + ", Soru Sayısı: " + quiz.getQuestions().size());
        
        return convertToStudentQuizDto(quiz);
    }
    
    
    @Transactional(readOnly = true)
    public List<QuizAttemptDto> getQuizAttemptsForStudent(int quizId, int studentId) {
        
        if (!userRepository.existsById(studentId)) {
            throw new UserNotFoundException("Öğrenci bulunamadı ID: " + studentId);
        }
        
       
        if (!quizRepository.existsById(quizId)) {
            throw new QuizNotFoundException("Quiz bulunamadı ID: " + quizId);
        }
        
        
        List<QuizSession> quizSessions = quizSessionRepository.findByQuizIdAndStudentId(quizId, studentId);
        
        
        return quizSessions.stream()
                .map(this::convertToQuizAttemptDto)
                .collect(Collectors.toList());
    }
    
    
    private QuizAttemptDto convertToQuizAttemptDto(QuizSession quizSession) {
        if (quizSession == null) {
            return null;
        }
        
       
        Quiz quiz = quizSession.getQuiz();
        int totalQuestions = (quiz != null && quiz.getQuestions() != null) ? 
                            quiz.getQuestions().size() : 0;
                            
      
        int passingScore = (quiz != null && quiz.getPassingScore() > 0) ? 
                          quiz.getPassingScore() : 
                          (int) Math.ceil(totalQuestions * 0.7);
        
       
        String teacherName = "Bilinmiyor";
        if (quiz != null && quiz.getTeacher() != null) {
            User teacher = quiz.getTeacher();
            teacherName = (teacher.getName() != null ? teacher.getName() + " " : "") +
                        (teacher.getSurname() != null ? teacher.getSurname() : "").trim();
        }
        
       
        String status = quizSession.isCompleted() ? "COMPLETED" : "IN_PROGRESS";
        
       
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
                .totalPoints(quiz != null ? quiz.getQuestions().stream().mapToInt(Question::getPoints).sum() : 0) 
                .earnedPoints(quizSession.getEarnedPoints())
                .build();
    }

   
    @Transactional(readOnly = true)
    public QuizWithQuestionsDto getQuizWithQuestions(int studentId, int quizId) {
        
        if (!userRepository.existsById(studentId)) {
            throw new UserNotFoundException("Öğrenci bulunamadı ID: " + studentId);
        }
        
        System.out.println("Öğrenci " + studentId + " için quiz getiriliyor. Quiz ID: " + quizId);
        
        
        List<Quiz> activeQuizzes = quizRepository.findActiveQuizzesWithQuestionsAndOptions();
        
        
        Quiz quiz = activeQuizzes.stream()
                .filter(q -> q.getId() == quizId)
                .findFirst()
                .orElseThrow(() -> {
                    System.out.println("Aktif quiz bulunamadı ID: " + quizId);
                    return new QuizNotFoundException("Bu quiz aktif değil veya bulunamadı ID: " + quizId);
                });
        
        
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            System.out.println("Quiz'de hiç soru yok. Quiz ID: " + quizId);
            throw new QuizNotFoundException("Bu quizde henüz soru bulunmuyor");
        }
        
        
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
    
   
    private StudentQuizDto convertToStudentQuizDto(Quiz quiz) {
        StudentQuizDto dto = new StudentQuizDto();
        dto.setId(quiz.getId());
        dto.setName(quiz.getName());
        dto.setDescription(quiz.getDescription());
        dto.setDurationMinutes(quiz.getDuration());
        dto.setActive(quiz.isActive()); 
        dto.setTopic(quiz.getTopic()); 
        
        dto.setQuestionCount(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0);

        
        if (quiz.getTeacher() != null) {
            StudentQuizDto.TeacherDto teacherDto = new StudentQuizDto.TeacherDto();
            teacherDto.setId(quiz.getTeacher().getId());
            teacherDto.setName(quiz.getTeacher().getName());
            teacherDto.setSurname(quiz.getTeacher().getSurname());
            dto.setTeacher(teacherDto);
        }

        return dto;
    }
    
  
   
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
            dto.setActive(quiz.isActive()); 
            
            
            List<Question> questions = quiz.getQuestions();
            if (questions == null || questions.isEmpty()) {
                System.out.println("Uyarı: Quiz'de hiç soru bulunamadı. Quiz ID: " + quiz.getId());
                dto.setQuestions(new ArrayList<>());
                return dto;
            }
            
            
            questions.sort((q1, q2) -> Integer.compare(q1.getNumber(), q2.getNumber()));
            System.out.println("Sorular numara sırasına göre sıralandı.");
            
            System.out.println("Toplam " + questions.size() + " soru işleniyor...");
            
            
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
                    
                    
                    int questionTypeId = (question.getType() != null) ? question.getType().getId() : 1;
                    questionDto.setQuestionTypeId(questionTypeId);
                    
                    
                    questionDto.setPoints(question.getPoints());
                    
                    
                    List<OptionDto> optionDtos = new ArrayList<>();
                    if (questionTypeId == 1 && question.getOptions() != null) { 
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
                    
                }
            }
            
            dto.setQuestions(questionDtos);
            System.out.println("Başarıyla " + questionDtos.size() + " soru dönüştürüldü.");
            return dto;
            
        } catch (Exception e) {
            System.err.println("Quiz DTO'ya dönüştürülürken beklenmeyen hata: " + e.getMessage());
            e.printStackTrace();
            throw e; 
        }
    }
    
   
    @Transactional
    public QuizSessionStartResponse startQuizSession(int quizId, int studentId) {
        System.out.println("Quiz oturumu başlatılıyor. Quiz ID: " + quizId + ", Öğrenci ID: " + studentId);
        
        try {
            
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
                
            
            Quiz quiz;
            try {
                System.out.println("Quiz veritabanından getiriliyor, ID: " + quizId);
                
                Optional<Quiz> quizOpt = quizRepository.findByIdWithQuestionsOnly(quizId);
                
                if (!quizOpt.isPresent()) {
                    System.out.println("Quiz bulunamadı ID: " + quizId);
                    throw new QuizNotFoundException("Quiz bulunamadı ID: " + quizId);
                }
                
                quiz = quizOpt.get();
                
                
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
            
            
            if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
                System.out.println("Quiz'de hiç soru bulunamadı. Quiz ID: " + quizId);
                throw new QuizNotFoundException("Bu quizde hiç soru bulunmuyor");
            }

            
            List<QuizSession> allSessions = quizSessionRepository.findByQuizIdAndStudentId(quizId, studentId);
            
            
            boolean hasCompletedSession = allSessions.stream()
                .anyMatch(session -> session.getEndTime() != null);
            
            if (hasCompletedSession) {
                System.out.println("Bu quiz zaten tamamlanmış. Quiz ID: " + quizId + ", Öğrenci ID: " + studentId);
                throw new RuntimeException("Bu quiz zaten tamamlanmış. Yeniden başlatılamaz.");
            }
            
            
            Optional<QuizSession> existingActiveSession = allSessions.stream()
                .filter(session -> session.getEndTime() == null)
                .findFirst();
                
            QuizSession session;
            
            if (existingActiveSession.isPresent()) {
                
                session = existingActiveSession.get();
                System.out.println("Mevcut aktif oturum kullanılıyor. Session ID: " + session.getId());
            } else {
                
                synchronized(this) {
                    
                    Optional<QuizSession> recheckSession = quizSessionRepository.findActiveSessionByStudentAndQuiz(studentId, quizId);
                    if (recheckSession.isPresent()) {
                        session = recheckSession.get();
                        System.out.println("Eşzamanlı istek tespit edildi, mevcut oturum kullanılıyor. Session ID: " + session.getId());
                    } else {
                        
                        session = new QuizSession(student, quiz);
                        session.setStartTime(LocalDateTime.now());
                        session = quizSessionRepository.save(session);
                        System.out.println("Yeni quiz oturumu oluşturuldu. Session ID: " + session.getId());
                    }
                }
            }
            
            
            System.out.println("Quiz bilgileri:");
            System.out.println("ID: " + quiz.getId());
            System.out.println("Name: " + (quiz.getName() != null ? quiz.getName() : "<isim yok>"));
            System.out.println("Questions size: " + quiz.getQuestions().size());
            
            
            for (int i = 0; i < quiz.getQuestions().size(); i++) {
                Question q = quiz.getQuestions().get(i);
                System.out.println("  Soru " + (i+1) + ": " + 
                    (q.getQuestionSentence() != null ? q.getQuestionSentence() : "<soru metni yok>"));
                
                
                if (q.getOptions() == null) {
                    q.setOptions(new ArrayList<>());
                }
                System.out.println("    Seçenek sayısı: " + q.getOptions().size());
            }
            
            System.out.println("Quiz oturumu başarıyla başlatıldı. Quiz ID: " + quizId);
            
            
            QuizWithQuestionsDto quizWithQuestions = convertToQuizWithQuestionsDto(quiz);
            System.out.println("Dönüştürülen DTO'da soru sayısı: " + 
                (quizWithQuestions.getQuestions() != null ? quizWithQuestions.getQuestions().size() : 0));
            
            
            
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
    
    
    @Transactional
    public QuizResultDto completeQuizSession(QuizCompletionRequestDto completionRequest, int studentId) {
        try {
            int quizId = completionRequest.getQuizId();
            
            
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz bulunamadı, ID: " + quizId));
            
            
            userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı, ID: " + studentId));
            
            System.out.println("Quiz ve öğrenci doğrulandı");
            
            
            List<QuizSession> sessions = quizSessionRepository.findByQuizIdAndStudentId(quizId, studentId);
            if (sessions.isEmpty()) {
                throw new RuntimeException("Bu quiz için aktif oturum bulunamadı. Lütfen önce quizi başlatın.");
            }
            
            
            QuizSession session = sessions.get(sessions.size() - 1);
            System.out.println("Quiz oturumu bulundu. Oturum ID: " + session.getId());
            
            
            if (session.getEndTime() != null) {
                throw new RuntimeException("Bu quiz oturumu zaten tamamlanmış.");
            }
            
            
            session.setEndTime(LocalDateTime.now());
            
            
            int totalQuestions = quiz.getQuestions().size();
            int correctAnswers = 0;
            List<QuizResultDto.QuestionResultDto> questionResults = new ArrayList<>();
            
            System.out.println("Toplam " + totalQuestions + " soru bulundu, işlenen cevap sayısı: " + completionRequest.getAnswers().size());
            
            
            for (QuizCompletionRequestDto.StudentAnswerDto answer : completionRequest.getAnswers()) {
                System.out.println("Cevap işleniyor - Soru ID: " + answer.getQuestionId() + 
                                 ", Seçilen Cevap ID: " + answer.getAnswerId() + 
                                 ", Metin Cevap: " + answer.getTextAnswer());
                
                
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
                
                
                boolean isCorrect = false;
                AnswerAttempt answerAttempt = new AnswerAttempt();
                answerAttempt.setQuizSession(session);
                answerAttempt.setQuestion(question);
                
                
                QuizResultDto.QuestionResultDto questionResult = new QuizResultDto.QuestionResultDto();
                questionResult.setQuestionId(question.getId());
                questionResult.setQuestionText(question.getQuestionSentence());
                
                
                System.out.println("Soru tipi: " + (question.getType() != null ? 
                    (question.getType().getId() == 1 ? "Çoktan Seçmeli" : "Açık Uçlu") : "Bilinmeyen"));
                
                
                if (question.getType() != null && question.getType().getId() == 1) {
                    
                    System.out.println("Çoktan seçmeli soru işleniyor. Seçilen cevap ID: " + answer.getAnswerId());
                    
                    
                    Option selectedOption = null;
                    if (answer.getAnswerId() != 0) { 
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
                    
                    
                    List<Option> correctOptions = question.getOptions().stream()
                            .filter(Option::isCorrect)
                            .collect(Collectors.toList());
                    
                    Option correctOption = correctOptions.isEmpty() ? null : correctOptions.get(0);
                    System.out.println("Doğru cevap: " + (correctOption != null ? 
                        (correctOption.getId() + " - " + correctOption.getText()) : "Tanımlı değil"));
                    
                    
                    if (selectedOption != null) {
                        
                        if (correctOption != null && selectedOption.getId() == correctOption.getId()) {
                            isCorrect = true;
                            correctAnswers++;
                            answerAttempt.setEarnedPoints(question.getPoints()); 
                        } else {
                            answerAttempt.setEarnedPoints(0); 
                        }
                        
                        
                        java.util.Set<Option> selectedOptions = new java.util.HashSet<>();
                        selectedOptions.add(selectedOption);
                        answerAttempt.setSelectedOptions(selectedOptions);
                        
                        
                        questionResult.setSelectedAnswerId(selectedOption.getId());
                    } else {
                        
                        answerAttempt.setSelectedOptions(new java.util.HashSet<>());
                        answerAttempt.setEarnedPoints(0); 
                        questionResult.setSelectedAnswerId(0); 
                    }
                    
                    
                    questionResult.setCorrectAnswerId(correctOption != null ? correctOption.getId() : 0);
                } else {
                    
                    String textAnswer = answer.getTextAnswer();
                    if (textAnswer == null) {
                        textAnswer = ""; 
                        System.out.println("Açık uçlu soru için cevap girilmemiş.");
                    } else {
                        System.out.println("Açık uçlu soru işleniyor. Cevap: " + textAnswer);
                    }
                    
                    
                    answerAttempt.setSubmittedAnswerText(textAnswer);
                    
                    
                    OpenEndedEvaluationResultDto evaluationResult = aiService.evaluateOpenEndedAnswer(
                        question.getQuestionSentence(), 
                        textAnswer, 
                        question.getCorrectAnswerText(),
                        question.getPoints()
                    );
                    
                    
                    System.out.println("AI Service returned - Points: " + evaluationResult.getEarnedPoints() + "/" + question.getPoints() + ", Explanation: " + evaluationResult.getExplanation());
                    
                    
                    isCorrect = evaluationResult.getEarnedPoints() > 0;
                    
                    
                    answerAttempt.setEarnedPoints(evaluationResult.getEarnedPoints());
                    
                    
                    answerAttempt.setAiExplanation(evaluationResult.getExplanation());
                    answerAttempt.setAiScore(evaluationResult.getEarnedPoints());
                    
                    
                    questionResult.setTextAnswer(textAnswer);
                    questionResult.setRequiresManualGrading(true);
                    questionResult.setEarnedPoints(evaluationResult.getEarnedPoints()); 
                    questionResult.setAiExplanation(evaluationResult.getExplanation());
                    questionResult.setAiScore(evaluationResult.getEarnedPoints());
                    
                    if (evaluationResult.getEarnedPoints() > 0) {
                        correctAnswers++;
                    }
                    
                    System.out.println("Açık uçlu soru işlendi - Cevap kaydedildi ve manuel değerlendirme gerekiyor");
                }
                
                answerAttempt.setCorrect(isCorrect);
                questionResult.setCorrect(isCorrect);
                
                session.addAnswerAttempt(answerAttempt);
                questionResults.add(questionResult);
            }
            
            int totalPossiblePoints = quiz.getQuestions().stream()
                    .mapToInt(Question::getPoints)
                    .sum();
            
            int earnedPoints = 0;
            
            for (AnswerAttempt attempt : session.getAnswers()) {
                earnedPoints += attempt.getEarnedPoints();
                System.out.println("Soru için kazanılan puan: " + attempt.getEarnedPoints() + ". Toplam puan: " + earnedPoints);
            }
            
           
            double scorePercentage = totalPossiblePoints > 0 ? 
                    (earnedPoints * 100.0) / totalPossiblePoints : 0;
                    
           
            session.setScore((int) Math.round(scorePercentage)); 
            session.setEarnedPoints(earnedPoints);
            session.setCorrectAnswers(correctAnswers);
            
            System.out.println(String.format("Quiz puanı hesaplandı: %.2f/100 (Puan: %d/%d, Doğru cevap: %d/%d)", 
                    scorePercentage, earnedPoints, totalPossiblePoints, correctAnswers, totalQuestions));
            
            try {
                quizSessionRepository.save(session);
                
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
                
                
                for (QuizResultDto.QuestionResultDto qr : questionResults) {
                    qr.setPoints(quiz.getQuestions().stream()
                            .filter(q -> q.getId() == qr.getQuestionId())
                            .findFirst()
                            .map(Question::getPoints)
                            .orElse(0));
                    
                    if (qr.getAiExplanation() != null && !qr.getAiExplanation().isEmpty()) { 
                        if (qr.getAiScore() != null && qr.getAiScore() > 0) {
                            qr.setEarnedPoints(qr.getAiScore());
                            System.out.println("Setting earned points for open-ended question to AI score: " + qr.getAiScore());
                        } else {
                            
                            System.out.println("Using existing earnedPoints for open-ended question: " + qr.getEarnedPoints());
                        }
                    } else { 
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
