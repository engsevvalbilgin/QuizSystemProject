package com.example.QuizSystemProject.Service;

import com.example.QuizSystemProject.Model.AnswerAttempt;
import com.example.QuizSystemProject.Model.Quiz;
import com.example.QuizSystemProject.Model.QuizSession;
import com.example.QuizSystemProject.Model.User;
import com.example.QuizSystemProject.Repository.AnswerAttemptRepository;
import com.example.QuizSystemProject.Repository.QuizRepository;
import com.example.QuizSystemProject.Repository.QuizSessionRepository;
import com.example.QuizSystemProject.Repository.UserRepository;
import com.example.QuizSystemProject.dto.OverallStatsResponse;
import com.example.QuizSystemProject.dto.QuizStatsResponse;
import com.example.QuizSystemProject.dto.StudentOverallResultsResponse;
import com.example.QuizSystemProject.exception.QuizNotFoundException;
import com.example.QuizSystemProject.exception.QuizSessionNotFoundException;
import com.example.QuizSystemProject.exception.UserNotAuthorizedException;
import com.example.QuizSystemProject.exception.UserNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Objects;

import java.util.stream.Collectors;

@Service 
@Transactional 
public class StatisticsService {

    private final QuizSessionRepository quizSessionRepository; 
    private final AnswerAttemptRepository answerAttemptRepository; 
    private final QuizRepository quizRepository; 
    private final UserRepository userRepository;

    public StatisticsService(QuizSessionRepository quizSessionRepository, AnswerAttemptRepository answerAttemptRepository,
                             QuizRepository quizRepository, UserRepository userRepository) {
        this.quizSessionRepository = quizSessionRepository;
        this.answerAttemptRepository = answerAttemptRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
    }

    
    @Transactional(readOnly = true) 
    public OverallStatsResponse getOverallProgramStatistics(int viewerUserId) {
        System.out.println("StatisticsService: Genel program istatistikleri getiriliyor - Izleyen ID: " + viewerUserId);

        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> { 
                    System.err.println("StatisticsService: Genel istatistikleri getirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        
        if (!"ROLE_ADMIN".equals(viewerUser.getRole())) { 
            System.err.println("StatisticsService: Genel istatistikleri getirirken - Kullanici Admin degil - ID: " + viewerUserId + ", Rol: " + viewerUser.getRole());
            throw new UserNotAuthorizedException("ID " + viewerUserId + " olan kullanıcının genel program istatistiklerini görme yetkisi yok."); 
        }

        
        int totalUsers = (int) userRepository.count(); 
        int totalQuizzes = (int) quizRepository.count(); 
        int totalQuizSessions = (int) quizSessionRepository.count(); 

        
        List<QuizSession> allSessions = quizSessionRepository.findAll(); 

        double averageScoreOverall = 0.0; 
        int completedSessionCount = 0; 

        if (allSessions != null && !allSessions.isEmpty()) {
             
             List<QuizSession> completedSessions = allSessions.stream()
                                                               .filter(session -> session.getScore() > 0) 
                                                               .collect(Collectors.toList()); 

             completedSessionCount = completedSessions.size(); 

             if (completedSessionCount > 0) {
                  
                  int totalScoreSum = completedSessions.stream()
                                                        .mapToInt(QuizSession::getScore) 
                                                        .sum(); 

      
                  averageScoreOverall = (double) totalScoreSum / completedSessionCount;
             }
        }

        
        OverallStatsResponse statsResponse = new OverallStatsResponse( 
            totalUsers,
            totalQuizzes,
            totalQuizSessions,
            averageScoreOverall
        );

        
       


        System.out.println("StatisticsService: Genel program istatistikleri başarıyla getirildi.");
        return statsResponse; 
    }


    @Transactional(readOnly = true) 
    public QuizStatsResponse getQuizStatistics(int quizId, int viewerUserId) {
        System.out.println("StatisticsService: Quiz istatistikleri getiriliyor - Quiz ID: " + quizId + ", Izleyen ID: " + viewerUserId);

        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Quiz istatistiklerini getirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Quiz istatistiklerini getirirken - Quiz bulunamadi - ID: " + quizId);
                    return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı.");
                });

        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole());
        boolean isTeacher = "ROLE_TEACHER".equals(viewerUser.getRole());
        boolean isQuizTeacher = isTeacher && quiz.getTeacher() != null && quiz.getTeacher().getId() == viewerUserId;

        if (!isQuizTeacher && !isAdmin) {
            System.err.println("StatisticsService: Quiz istatistiklerini getirirken - Kullanici yetkisiz - Quiz ID: " + quizId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("Bu quizin istatistiklerini görme yetkiniz yok. Sadece quizi oluşturan öğretmen veya admin görebilir.");
        }

        List<QuizSession> quizSessions = quizSessionRepository.findAllByQuiz(quiz); 

        List<QuizSession> completedSessions = quizSessions.stream()
                                                         .filter(session -> session.getScore() > 0)
                                                         .collect(Collectors.toList());

        long totalAttempts = completedSessions.size(); 

        Double averageScore = 0.0;
        Integer highestScore = null; 
        Integer lowestScore = null;

        if (totalAttempts > 0) {
             IntSummaryStatistics scoreStats = completedSessions.stream()
                                                               .mapToInt(QuizSession::getScore) 
                                                               .summaryStatistics(); 

             averageScore = scoreStats.getAverage(); 
             highestScore = scoreStats.getMax(); 
             lowestScore = scoreStats.getMin(); 
        } else {
             System.out.println("StatisticsService: Quiz ID " + quizId + " icin tamamlanmis oturum bulunamadi.");
        }
        long totalQuestions = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0; 


        QuizStatsResponse statsResponse = new QuizStatsResponse( 
                quiz.getId(), 
                quiz.getName(), 
                totalAttempts, 
                averageScore, 
                highestScore, 
                lowestScore, 
                totalQuestions 
            );


        System.out.println("StatisticsService: Quiz istatistikleri başarıyla getirildi - Quiz ID: " + quizId);
        return statsResponse; 
    }


    @Transactional(readOnly = true) 
    public List<AnswerAttempt> getQuizAnswersReview(int quizId, int viewerUserId) {
        System.out.println("StatisticsService: Quiz cevapları gözden geçiriliyor - Quiz ID: " + quizId + ", Izleyen ID: " + viewerUserId);

       
        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Quiz cevaplarını gözden geçirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

      
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Quiz cevaplarını gözden geçirirken - Quiz bulunamadi - ID: " + quizId);
                    return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı.");
                });

        boolean isQuizTeacher = quiz.getTeacher() != null && quiz.getTeacher().getId() == viewerUserId;
        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole()); 

        if (!isQuizTeacher && !isAdmin) {
            System.err.println("StatisticsService: Quiz cevaplarını gözden geçirirken - Kullanici yetkisiz - Quiz ID: " + quizId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("ID " + viewerUserId + " olan kullanıcının bu quize ait cevapları gözden geçirme yetkisi yok.");
        }

        List<AnswerAttempt> answerAttempts = answerAttemptRepository.findByQuizSession_Quiz_Id(quizId); 

        System.out.println("StatisticsService: Quiz ID " + quizId + " icin " + answerAttempts.size() + " adet cevap denemesi bulundu.");
        return answerAttempts; 
    }

    @Transactional(readOnly = true) 
    public List<QuizSession> getStudentOverallResults(int studentId, int viewerUserId) {
        System.out.println("StatisticsService: Öğrenci genel sonuçları getiriliyor - Öğrenci ID: " + studentId + ", Izleyen ID: " + viewerUserId);

      
        User targetStudent = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrenci genel sonuçlarını getirirken - Hedef öğrenci bulunamadi - ID: " + studentId);
                    return new UserNotFoundException("ID " + studentId + " olan öğrenci bulunamadı.");
                });

        if (!"ROLE_STUDENT".equals(targetStudent.getRole())) {
             System.err.println("StatisticsService: Öğrenci genel sonuçlarını getirirken - Hedef kullanici ogrenci degil - ID: " + studentId + ", Rol: " + targetStudent.getRole());
             throw new UserNotAuthorizedException("ID " + studentId + " olan kullanıcı bir öğrenci değil. Sadece öğrencilerin sonuçları bu endpoint'ten getirilebilir.");
        }


    
        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrenci genel sonuçlarını getirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        
        boolean isSelf = viewerUser.getId() == studentId;
        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole());
        boolean isTeacher = "ROLE_TEACHER".equals(viewerUser.getRole());
        if (isTeacher) {
            List<QuizSession> studentSessions = quizSessionRepository.findAllByStudent(targetStudent);
            boolean hasTeacherQuizSession = studentSessions.stream()
                .anyMatch(session -> session.getQuiz().getTeacher() != null && 
                         session.getQuiz().getTeacher().getId() == viewerUserId);

            if (!hasTeacherQuizSession) {
                System.err.println("StatisticsService: Öğrenci genel sonuçlarını getirirken - Öğretmen yetkisiz - Öğrenci ID: " + studentId + ", Öğretmen ID: " + viewerUserId);
                throw new UserNotAuthorizedException("Bu öğrenci sizin quizlerinize ait herhangi bir oturum gerçekleştirmemiş.");
            }
        }

        if (!isSelf && !isAdmin && !isTeacher) {
            System.err.println("StatisticsService: Öğrenci genel sonuçlarını getirirken - Kullanici yetkisiz - Hedef Öğrenci ID: " + studentId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("ID " + viewerUserId + " olan kullanıcının ID " + studentId + " olan öğrencinin sonuçlarını görme yetkisi yok.");
        }


        List<QuizSession> sessions = quizSessionRepository.findAllByStudent(targetStudent);

        if (isTeacher && !isAdmin) {
            sessions = sessions.stream()
                .filter(session -> session.getQuiz().getTeacher() != null && 
                        session.getQuiz().getTeacher().getId() == viewerUserId)
                .collect(Collectors.toList());
        }

        System.out.println("StatisticsService: Öğrenci ID " + studentId + " icin " + sessions.size() + " adet oturum bulundu.");
        return sessions; 
    }

 
    
    @Transactional(readOnly = true) 
    public QuizSession getStudentQuizResult(int studentId, int quizId, int viewerUserId) { 
        System.out.println("StatisticsService: Öğrencinin quiz sonucu getiriliyor - Öğrenci ID: " + studentId + ", Quiz ID: " + quizId + ", Izleyen ID: " + viewerUserId);

        User targetStudent = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Hedef öğrenci bulunamadi - ID: " + studentId);
                    return new UserNotFoundException("ID " + studentId + " olan öğrenci bulunamadı.");
                });

        if (!"ROLE_STUDENT".equals(targetStudent.getRole())) {
             System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Hedef kullanici ogrenci degil - ID: " + studentId + ", Rol: " + targetStudent.getRole());
             throw new UserNotAuthorizedException("ID " + studentId + " olan kullanıcı bir öğrenci değil.");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Quiz bulunamadi - ID: " + quizId);
                    return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı.");
                });

        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        boolean isSelf = viewerUser.getId() == studentId;
        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole());
        boolean isTeacher = "ROLE_TEACHER".equals(viewerUser.getRole());
        if (isTeacher && !isAdmin) {
            boolean isQuizTeacher = quiz.getTeacher() != null && quiz.getTeacher().getId() == viewerUserId;
            if (!isQuizTeacher) {
                System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Öğretmen yetkisiz - Quiz ID: " + quizId + ", Öğretmen ID: " + viewerUserId);
                throw new UserNotAuthorizedException("Bu quizin sonuçlarını görme yetkiniz yok. Sadece quizi oluşturan öğretmen görebilir.");
            }
        }

        if (!isSelf && !isAdmin && !isTeacher) {
            System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Kullanici yetkisiz - Hedef Öğrenci ID: " + studentId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("Bu öğrencinin quiz sonucunu görme yetkiniz yok.");
        }

        QuizSession quizSession = quizSessionRepository.findByStudentAndQuiz(targetStudent, quiz)
            .orElseThrow(() -> {
                System.err.println("StatisticsService: Öğrencinin quiz sonucunu getirirken - Oturum bulunamadi - Öğrenci ID: " + studentId + ", Quiz ID: " + quizId);
                return new QuizSessionNotFoundException("ID " + studentId + " olan öğrencinin ID " + quizId + " olan quize ait oturumu bulunamadı.");
            });

        return quizSession;
    }

 
    @Transactional(readOnly = true) 
    public double calculateAverageScoreForStudent(int studentId, int viewerUserId) { 
        System.out.println("StatisticsService: Öğrenci ortalama puanı hesaplanıyor - Öğrenci ID: " + studentId + ", Izleyen ID: " + viewerUserId);

        User targetStudent = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrenci ortalama puanını hesaplarken - Hedef öğrenci bulunamadi - ID: " + studentId);
                    return new UserNotFoundException("ID " + studentId + " olan öğrenci bulunamadı.");
                });

        if (!"ROLE_STUDENT".equals(targetStudent.getRole())) {
             System.err.println("StatisticsService: Öğrenci ortalama puanını hesaplarken - Hedef kullanici ogrenci degil - ID: " + studentId + ", Rol: " + targetStudent.getRole());
             throw new UserNotAuthorizedException("ID " + studentId + " olan kullanıcı bir öğrenci değil. Sadece öğrencilerin ortalama puanı bu endpoint'ten getirilebilir.");
        }

        User viewerUser = userRepository.findById(viewerUserId)
                .orElseThrow(() -> {
                    System.err.println("StatisticsService: Öğrenci ortalama puanını hesaplarken - Izleyen kullanici bulunamadi - ID: " + viewerUserId);
                    return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı.");
                });

        boolean isSelf = viewerUser.getId() == studentId;
        boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole());
        boolean isTeacher = "ROLE_TEACHER".equals(viewerUser.getRole());
        if (isTeacher && !isAdmin) {
            List<QuizSession> studentSessions = quizSessionRepository.findAllByStudent(targetStudent);
            boolean hasTeacherQuizSession = studentSessions.stream()
                .anyMatch(session -> session.getQuiz().getTeacher() != null && 
                         session.getQuiz().getTeacher().getId() == viewerUserId);

            if (!hasTeacherQuizSession) {
                System.err.println("StatisticsService: Öğrenci ortalama puanını hesaplarken - Öğretmen yetkisiz - Öğrenci ID: " + studentId + ", Öğretmen ID: " + viewerUserId);
                throw new UserNotAuthorizedException("Bu öğrencinin ortalama puanını görme yetkiniz yok. Sadece kendi quizlerinize ait sonuçları görebilirsiniz.");
            }
        }

        if (!isSelf && !isAdmin && !isTeacher) {
            System.err.println("StatisticsService: Öğrenci ortalama puanını hesaplarken - Kullanici yetkisiz - Hedef Öğrenci ID: " + studentId + ", Izleyen ID: " + viewerUserId);
            throw new UserNotAuthorizedException("ID " + viewerUserId + " olan kullanıcının ID " + studentId + " olan öğrencinin ortalama puanını görme yetkisi yok.");
        }

        List<QuizSession> studentSessions = quizSessionRepository.findAllByStudent(targetStudent);

        if (isTeacher && !isAdmin) {
            studentSessions = studentSessions.stream()
                .filter(session -> session.getQuiz().getTeacher() != null && 
                        session.getQuiz().getTeacher().getId() == viewerUserId)
                .collect(Collectors.toList());
        }

        double averageScore = studentSessions.stream()
            .filter(session -> session.getScore() > 0) 
            .mapToInt(QuizSession::getScore)
            .average()
            .orElse(0.0); 

        System.out.println("StatisticsService: Öğrenci ID " + studentId + " için ortalama puan: " + averageScore);
        return averageScore;
    }

   
    @Transactional(readOnly = true)
    public List<StudentOverallResultsResponse> getStudentLeaders() {
        System.out.println("StatisticsService: Öğrenci liderlik tablosu hazırlanıyor");
        
        List<User> students = userRepository.findAllByRole("ROLE_STUDENT");
        
        List<StudentOverallResultsResponse> leaders = students.stream()
            .map(student -> {
                try {
                    List<QuizSession> sessions = quizSessionRepository.findByStudentId(student.getId());
                    return new StudentOverallResultsResponse(student, sessions);
                } catch (Exception e) {
                    System.err.println("Öğrenci puanı hesaplanırken hata - Öğrenci ID: " + student.getId() + ", Hata: " + e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull) 
            .sorted(Comparator.comparingDouble(StudentOverallResultsResponse::getAverageScore).reversed()) 
            .collect(Collectors.toList());
            
        System.out.println("StatisticsService: Toplam " + leaders.size() + " öğrenci listelendi");
        return leaders;
    }
    
}
