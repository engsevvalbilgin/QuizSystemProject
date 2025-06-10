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



@Service 
@Transactional 
public class QuizSessionService {

    private final QuizSessionRepository quizSessionRepository;
    private final AnswerAttemptRepository answerAttemptRepository; 
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository;

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

   
    @Transactional
    public QuizResultDto completeQuizSession(int sessionId, int studentId) {
        System.out.println("QuizSessionService: Quiz oturumu tamamlama istegi - Oturum ID: " + sessionId + ", Ogrenci ID: " + studentId);
        
        QuizSession session = quizSessionRepository.findById(sessionId)
            .orElseThrow(() -> {
                System.err.println("QuizSessionService: Oturum bulunamadi - ID: " + sessionId);
                return new RuntimeException("Quiz oturumu bulunamadı: " + sessionId);
            });
            
        if (session.getStudent().getId() != studentId) {
            System.err.println("QuizSessionService: Yetkisiz erisim denemesi - Oturum ID: " + sessionId + ", Istek Yapan ID: " + studentId);
            throw new RuntimeException("Bu sınava erişim yetkiniz yok");
        }
        
     
        if (session.isCompleted()) {
            System.err.println("QuizSessionService: Oturum zaten tamamlanmis - Oturum ID: " + sessionId);
            throw new RuntimeException("Bu sınav zaten tamamlanmış");
        }
        
        session.setCompleted(true);
        session.setEndTime(LocalDateTime.now());
        
        long timeSpentSeconds = Duration.between(session.getStartTime(), session.getEndTime()).getSeconds();
        session.setTimeSpentSeconds((int) timeSpentSeconds);
        
        List<AnswerAttempt> answers = answerAttemptRepository.findByQuizSessionId(sessionId);
        System.out.println("QuizSessionService: " + answers.size() + " adet cevap bulundu");
        
        int correctCount = 0;
        int totalScore = 0;
        int maxPossibleScore = 0;
        
        for (AnswerAttempt answer : answers) {
            Question question = answer.getQuestion();
            maxPossibleScore += question.getPoints();
            
            
            System.out.println("QuizSessionService: İşleniyor - Soru ID: " + question.getId() + 
                ", Tip: " + question.getType().getTypeName() + ", Max Puan: " + question.getPoints());
            
            if ("OPEN_ENDED".equals(question.getType().getTypeName())) {
                int earnedPoints = answer.getEarnedPoints();
                System.out.println("QuizSessionService: Açık uçlu soru - Kazanılan Puan: " + earnedPoints + 
                    ", AI Puanı: " + answer.getAiScore());
                
                if (answer.getAiScore() > 0) {
                    totalScore += answer.getAiScore();
                    if (answer.getAiScore() > 0) {
                        correctCount++;
                    }
                    System.out.println("QuizSessionService: AI puanı kullanılıyor: " + answer.getAiScore());
                } else {
                    totalScore += earnedPoints;
                    if (earnedPoints > 0) {
                        correctCount++;
                    }
                    System.out.println("QuizSessionService: Kazanılan puan kullanılıyor: " + earnedPoints);
                }
            } else { 
                if (answer.isCorrect()) {
                    correctCount++;
                    totalScore += question.getPoints();
                    System.out.println("QuizSessionService: Çoktan seçmeli soru doğru - Puan: " + question.getPoints());
                } else {
                    System.out.println("QuizSessionService: Çoktan seçmeli soru yanlış - Puan: 0");
                }
            }
        }
        
        session.setCorrectAnswers(correctCount);
        int scorePercentage = maxPossibleScore > 0 ? (int) ((totalScore * 100.0) / maxPossibleScore) : 0;
        session.setScore(scorePercentage); 
        session.setEarnedPoints(totalScore);
        
        quizSessionRepository.save(session);
        
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
        
        List<QuizResultDto.QuestionResultDto> questionResults = new ArrayList<>();
        for (AnswerAttempt answer : answers) {
            QuizResultDto.QuestionResultDto qr = new QuizResultDto.QuestionResultDto();
            qr.setQuestionId(answer.getQuestion().getId());
            qr.setQuestionText(answer.getQuestion().getQuestionSentence());
            qr.setQuestionTypeId(answer.getQuestion().getType().getTypeName()); 
            
            if (answer.getQuestion().getType().getTypeName().equals("MULTIPLE_CHOICE")) {
                qr.setSelectedAnswerId(answer.getSelectedOptions() != null && !answer.getSelectedOptions().isEmpty() ? answer.getSelectedOptions().iterator().next().getId() : null);
                qr.setCorrect(answer.isCorrect());
                qr.setEarnedPoints(answer.getEarnedPoints());
            } else if (answer.getQuestion().getType().getTypeName().equals("OPEN_ENDED")) {
                qr.setTextAnswer(answer.getTextAnswer());
                qr.setRequiresManualGrading(answer.getAiScore() == 0); 
                qr.setEarnedPoints(answer.getAiScore()); 
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
    
    @Transactional 
    public QuizSession startQuizSession(int studentId, int quizId) {
        System.out.println("QuizSessionService: Quiz oturumu baslatma istegi - Ogrenci ID: " + studentId + ", Quiz ID: " + quizId);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Oturum baslatma - Ogrenci bulunamadi - ID: " + studentId);
                    return new UserNotFoundException("ID " + studentId + " olan öğrenci bulunamadı.");
                });

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Oturum baslatma - Quiz bulunamadi - ID: " + quizId);
                    return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı.");
                });

        if (!"ROLE_STUDENT".equals(student.getRole())) {
             System.err.println("QuizSessionService: Oturum baslatma - Kullanici ogrenci degil - ID: " + studentId + ", Rol: " + student.getRole());
             throw new UserNotAuthorizedException("ID " + studentId + " olan kullanıcı bir öğrenci değil. Sadece öğrenciler quiz oturumu başlatabilir.");
        }

        
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
        

        QuizSession newSession = new QuizSession(student, quiz);

        QuizSession savedSession = quizSessionRepository.save(newSession);

        System.out.println("QuizSessionService: Quiz oturumu baslatildi - Oturum ID: " + savedSession.getId());
        return savedSession;
    }


    @Transactional 
    public AnswerAttempt submitAnswer(int sessionId, int questionId, String submittedAnswerText, Set<Integer> selectedOptionIds, int submitterUserId) {
        System.out.println("QuizSessionService: Cevap gonderme istegi - Oturum ID: " + sessionId + ", Soru ID: " + questionId);

        
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Cevap gonderme - Oturum bulunamadi - ID: " + sessionId);
                    return new QuizSessionNotFoundException("ID " + sessionId + " olan quiz oturumu bulunamadı.");
                });

        if (session.getStudent() == null || session.getStudent().getId() != submitterUserId) {
            System.err.println("QuizSessionService: Cevap gonderme - Kullanici oturum sahibi degil veya ogrenci bilgisi eksik - Oturum ID: " + sessionId + ", Gonderen ID: " + submitterUserId);
            throw new UserNotAuthorizedException("ID " + submitterUserId + " olan kullanıcının ID " + sessionId + " olan oturuma cevap gönderme yetkisi yok.");
        }

        
        if (session.getEndTime() != null) {
             System.err.println("QuizSessionService: Cevap gonderme - Oturum zaten tamamlanmis - ID: " + sessionId);
             throw new QuizSessionExpiredException("ID " + sessionId + " olan quiz oturumu zaten tamamlanmış."); 
        }

        
         Quiz quiz = session.getQuiz(); 
         Integer duration = (quiz != null) ? quiz.getDuration() : null; 
         if (quiz != null && duration != null && session.getStartTime() != null) { 
              LocalDateTime sessionEndTimeLimit = session.getStartTime().plusMinutes(duration); 
              if (LocalDateTime.now().isAfter(sessionEndTimeLimit)) {
                   System.err.println("QuizSessionService: Cevap gonderme - Oturum suresi dolmus - ID: " + sessionId);
                   throw new QuizSessionExpiredException("ID " + sessionId + " olan quiz oturum süreniz dolmuştur. Lütfen oturumu tamamlayın.");
              }
         }


        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Cevap gonderme - Soru bulunamadi - ID: " + questionId);
                    return new QuestionNotFoundException("ID " + questionId + " olan soru bulunamadı.");
                });

        
        if (question.getQuiz() == null || session.getQuiz().getId() != question.getQuiz().getId()) {
           System.err.println("QuizSessionService: Cevap gonderme - Soru oturumun quizine ait degil - Soru ID: " + questionId + ", Oturum Quiz ID: " + session.getQuiz().getId());
           throw new QuestionDoesNotBelongToQuizException("Soru ID " + questionId + " , Quiz oturumu ID " + sessionId + "'nin quizine ait değil.");
        }


        
        answerAttemptRepository.findByQuizSessionAndQuestion(session, question).ifPresent(existingAttempt -> {
             System.err.println("QuizSessionService: Cevap gonderme - Soruya zaten cevap verilmis - Oturum ID: " + sessionId + ", Soru ID: " + questionId);
             throw new AnswerAlreadySubmittedException("ID " + questionId + " olan soruya zaten cevap verilmiş.");
        });


        AnswerAttempt newAttempt;
        QuestionType questionType = question.getType();

        if (questionType == null) {
             System.err.println("QuizSessionService: Cevap gonderme - Soru tipi bulunamadi veya null - Soru ID: " + questionId);
             throw new IllegalStateException("Soru ID " + questionId + " için soru tipi tanımlı değil."); 
        }

        
        if ("Açık Uçlu".equals(questionType.getTypeName()) || "Kısa Cevap".equals(questionType.getTypeName())) {
            
            if (selectedOptionIds != null && !selectedOptionIds.isEmpty()) {
                 System.err.println("QuizSessionService: Cevap gonderme - Metin sorusu icin secili sik gonderildi - Soru ID: " + questionId);
                 throw new InvalidQuestionTypeForAnswerException("Metin tabanlı soru için şık gönderilemez.");
            }
            newAttempt = new AnswerAttempt(session, question, submittedAnswerText); 

        } else if ("Çoktan Seçmeli".equals(questionType.getTypeName())) {
             if (submittedAnswerText != null && !submittedAnswerText.trim().isEmpty()) {
                 System.err.println("QuizSessionService: Cevap gonderme - Coktan secmeli soru icin metin cevap gonderildi - Soru ID: " + questionId);
                 throw new InvalidQuestionTypeForAnswerException("Çoktan seçmeli soru için metin cevap gönderilemez.");
             }
             if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
                 System.err.println("QuizSessionService: Cevap gonderme - Coktan secmeli soru icin secili sik gonderilmedi - Soru ID: " + questionId);
                 throw new InvalidQuestionTypeForAnswerException("Çoktan seçmeli soru için en az bir şık seçilmelidir.");
             }

            Set<Option> selectedOptions = new HashSet<>();
            selectedOptionIds.forEach(optionId -> {
                 Option option = optionRepository.findById(optionId)
                         .orElseThrow(() -> {
                              System.err.println("QuizSessionService: Cevap gonderme - Secilen sik bulunamadi - Option ID: " + optionId);
                              return new OptionNotFoundException("ID " + optionId + " olan şık bulunamadı.");
                         });
                 if (option.getQuestion() == null || option.getQuestion().getId() != questionId) {
                     System.err.println("QuizSessionService: Cevap gonderme - Secilen sik soruya ait degil - Sik ID: " + optionId + ", Soru ID: " + questionId);
                     throw new InvalidOptionForQuestionException("Şık ID " + optionId + " , Soru ID " + questionId + "'e ait değil.");
                 }
                 selectedOptions.add(option);
            });
            newAttempt = new AnswerAttempt(session, question, selectedOptions); 

        } else {
            System.err.println("QuizSessionService: Cevap gonderme - Bilinmeyen veya desteklenmeyen soru tipi - Soru ID: " + questionId + ", Tip: " + questionType.getTypeName());
            throw new InvalidQuestionTypeForAnswerException("Bilinmeyen veya desteklenmeyen soru tipi."); 
        }

        
        boolean isCorrect = checkAnswerCorrectness(question, newAttempt); 

        
        newAttempt.setCorrect(isCorrect);

        
        session.addAnswerAttempt(newAttempt);

        
        quizSessionRepository.save(session);

        System.out.println("QuizSessionService: Cevap basariyla kaydedildi - Attempt ID: " + newAttempt.getId() + ", Dogru Mu: " + newAttempt.isCorrect());
        return newAttempt; 
    }



    

    
    private boolean checkAnswerCorrectness(Question question, AnswerAttempt answerAttempt) {
        System.out.println("QuizSessionService: Cevap dogrulugu kontrol ediliyor - Soru ID: " + question.getId() + ", Attempt ID: " + answerAttempt.getId());

       
        QuestionType questionType = question.getType();
         if (questionType == null) {
             System.err.println("QuizSessionService: checkAnswerCorrectness - Soru tipi yuklenemedi veya null - Soru ID: " + question.getId());
             return false;
         }

        String typeName = questionType.getTypeName();

        if ("Açık Uçlu".equals(typeName) || "Kısa Cevap".equals(typeName)) {
            
            QuestionAnswer correctAnswerEntity = question.getAnswer();
            String correctText = (correctAnswerEntity != null) ? correctAnswerEntity.getAnswer() : null;
            String submittedText = answerAttempt.getSubmittedAnswerText();

            
            if (correctText == null || correctText.trim().isEmpty()) {
                 
                 System.out.println("QuizSessionService: checkAnswerCorrectness - Metin tabanli soru (Acik Uclu/Kisa Cevap) - Dogru cevap metni tanimli degil. ID: " + question.getId());
                 return false; 
            }
            if (submittedText == null || submittedText.trim().isEmpty()) {
                 
                 System.out.println("QuizSessionService: checkAnswerCorrectness - Metin tabanli soru (Acik Uclu/Kisa Cevap) - Gonderilen cevap metni bos. ID: " + question.getId());
                 return false; 
            }


            
            boolean isMatch = submittedText.trim().equalsIgnoreCase(correctText.trim());
             System.out.println("QuizSessionService: checkAnswerCorrectness - Metin tabanli soru (Acik Uclu/Kisa Cevap) - ID: " + question.getId() + ", Dogru Cevap: '" + correctText + "', Gonderilen Cevap: '" + submittedText + "', Eslesme: " + isMatch);
            return isMatch;

        } else if ("Çoktan Seçmeli".equals(typeName)) {
            
            Set<Option> selectedOptions = answerAttempt.getSelectedOptions(); 
            Set<Option> correctOptions = new HashSet<>(); 

            
            if (question.getOptions() != null) {
                question.getOptions().stream()
                        .filter(Option::isCorrect)
                        .forEach(correctOptions::add);
            }

             
             boolean isMatch = selectedOptions != null && selectedOptions.equals(correctOptions);

             System.out.println("QuizSessionService: checkAnswerCorrectness - Coktan secmeli soru - ID: " + question.getId() + ", Eslesme: " + isMatch);
            return isMatch;

        } else {
            
            System.err.println("QuizSessionService: checkAnswerCorrectness - Bilinmeyen veya desteklenmeyen soru tipi - ID: " + question.getId() + ", Tip: " + typeName);
            return false; 
        }
    }

 
    @Deprecated
    private int calculateScore(QuizSession session) {
        System.out.println("QuizSessionService: Puan hesaplaniyor - Oturum ID: " + session.getId());
        int totalScore = 0;

        
        List<AnswerAttempt> attempts = session.getAnswers(); 

        if (attempts != null && !attempts.isEmpty()) {
             for (AnswerAttempt attempt : attempts) {
                
                if (attempt.isCorrect()) {
                    
                
                    totalScore += 10; 
                }
            }
        } else {
             System.out.println("QuizSessionService: Puan hesaplaniyor - Oturum ID: " + session.getId() + ", Cevap bulunamadi.");
        }


        System.out.println("QuizSessionService: Puan hesaplandi - Oturum ID: " + session.getId() + ", Hesaplanan Puan: " + totalScore);
        return totalScore;
    }
 
    @Transactional(readOnly = true) 
    public Duration calculateDuration(int sessionId) { 
        System.out.println("QuizSessionService: Sure hesaplaniyor - Oturum ID: " + sessionId);

         
         QuizSession session = quizSessionRepository.findById(sessionId)
                 .orElseThrow(() -> {
                     System.err.println("QuizSessionService: Sure hesaplaniyor - Oturum bulunamadi - ID: " + sessionId);
                     return new QuizSessionNotFoundException("ID " + sessionId + " olan quiz oturumu bulunamadı.");
                 });


         if (session.getStartTime() == null) {
             System.err.println("QuizSessionService: Sure hesaplaniyor - Baslangic zamani bos - Oturum ID: " + sessionId);
             
             return Duration.ZERO;
         }

         
         
         LocalDateTime endTime = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now();

         
         Duration duration = Duration.between(session.getStartTime(), endTime);

         System.out.println("QuizSessionService: Sure hesaplandi - Oturum ID: " + sessionId + ", Sure: " + duration);
         return duration;
    }
    
    @Transactional(readOnly = true)
    public QuizSession getQuizSessionDetails(int sessionId, int viewerUserId) { 
        System.out.println("QuizSessionService: Oturum detaylari getiriliyor - Oturum ID: " + sessionId + ", Izleyen ID: " + viewerUserId);

        
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Oturum detaylari getirirken - Oturum bulunamadi - ID: " + sessionId);
                    return new QuizSessionNotFoundException("ID " + sessionId + " olan quiz oturumu bulunamadı.");
                });

        
        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Oturum detaylari getirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        
        boolean isSessionOwner = session.getStudent() != null && session.getStudent().getId() != viewerUserId;
        boolean isQuizTeacher = session.getQuiz() != null && session.getQuiz().getTeacher() != null && session.getQuiz().getTeacher().getId() != viewerUserId;
        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole()); 

        if (!isSessionOwner && !isQuizTeacher && !isAdmin) {
            System.err.println("QuizSessionService: Oturum detaylari getirirken - Kullanici yetkisiz - Oturum ID: " + sessionId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("ID " + viewerUserId + " olan kullanıcının bu oturumun detaylarını görme yetkisi yok.");
        }

        
        System.out.println("QuizSessionService: Oturum detaylari bulundu ve yetki kontrolu gecti - Oturum ID: " + sessionId);
        return session;
    }

    
    @Transactional
    public boolean saveAnswer(int sessionId, int studentId, StudentAnswerDto answerDto) {
        
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new QuizSessionNotFoundException("ID " + sessionId + " olan quiz oturumu bulunamadı."));
        
        
        if (session.getStudent() == null || session.getStudent().getId() != studentId) {
            throw new UserNotAuthorizedException("Bu oturumun sahibi değilsiniz.");
        }
        
        
        if (session.isSubmitted()) {
            throw new QuizSessionCompletedException("Bu quiz oturumu zaten tamamlanmış.");
        }
        
        
        Question question = questionRepository.findById(answerDto.getQuestionId())
                .orElseThrow(() -> new QuestionNotFoundException("ID " + answerDto.getQuestionId() + " olan soru bulunamadı."));
        
        
        AnswerAttempt attempt = new AnswerAttempt();
        attempt.setQuestion(question);
        attempt.setQuizSession(session);
        
        if (answerDto.getAnswerType() == AnswerType.MULTIPLE_CHOICE) {
            if (answerDto.getSelectedOptionIds() != null && !answerDto.getSelectedOptionIds().isEmpty()) {
                Set<Option> correctOptions = question.getOptions().stream()
                        .filter(Option::isCorrect)
                        .collect(Collectors.toSet());
                
                Set<Option> selectedOptions = new HashSet<>();
                for (Integer optionId : answerDto.getSelectedOptionIds()) {
                    Option selectedOption = optionRepository.findById(optionId)
                            .orElseThrow(() -> new RuntimeException("Seçenek bulunamadı: " + optionId));
                    selectedOptions.add(selectedOption);
                    attempt.addSelectedOption(selectedOption);
                }
                
                boolean isCorrect = selectedOptions.containsAll(correctOptions) && 
                                  selectedOptions.size() == correctOptions.size();
                attempt.setCorrect(isCorrect);
                attempt.setEarnedPoints(isCorrect ? question.getPoints() : 0);
            }
        } else if (answerDto.getAnswerType() == AnswerType.TEXT) {
            
            attempt.setTextAnswer(answerDto.getTextAnswer());
            
            if (answerDto.getScore() > 0) {
                
                attempt.setAiScore((int) answerDto.getScore());
                attempt.setEarnedPoints((int) answerDto.getScore());
                
                attempt.setCorrect(true);
                
                if (answerDto.getAiExplanation() != null) {
                    attempt.setAiExplanation(answerDto.getAiExplanation());
                    System.out.println("QuizSessionService: AI açıklaması kaydedildi: " + answerDto.getAiExplanation());
                }
            } else {
             
                attempt.setEarnedPoints(0);
                attempt.setCorrect(false);
            }
        }
        
      
        answerAttemptRepository.save(attempt);
        
        return true;
    }
    
   
    @Transactional(readOnly = true)
    public Question getQuestionById(int questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("ID " + questionId + " olan soru bulunamadı."));
    }
    
    
    @Transactional(readOnly = true) 
    public List<QuizSession> getStudentQuizSessions(int studentId) {
        System.out.println("QuizSessionService: Öğrenci oturumlari getiriliyor - Öğrenci ID: " + studentId);

        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    System.err.println("QuizSessionService: Ogrenci oturumlarini getirirken - Ogrenci bulunamadi - ID: " + studentId);
                    return new UserNotFoundException("ID " + studentId + " olan öğrenci bulunamadı.");
                });

        
         if (!"ROLE_STUDENT".equals(student.getRole())) {
             System.err.println("QuizSessionService: Ogrenci oturumlarini getirirken - Kullanici ogrenci degil - ID: " + studentId + ", Rol: " + student.getRole());
             throw new UserNotAuthorizedException("ID " + studentId + " olan kullanıcı bir öğrenci değil.");
         }

      
        List<QuizSession> sessions = quizSessionRepository.findAllByStudent(student);

        System.out.println("QuizSessionService: Ogrenci ID " + studentId + " icin " + sessions.size() + " adet oturum bulundu.");
        return sessions;
    }

}