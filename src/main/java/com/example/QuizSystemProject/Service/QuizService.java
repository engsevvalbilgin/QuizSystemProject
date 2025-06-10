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

@Service 
@Transactional 
public class QuizService implements ApplicationContextAware {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository; 
    private final QuestionTypeRepository questionTypeRepository; 
    private TeacherRepository teacherRepository; 

        private ApplicationContext applicationContext;
    
    
    
    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository,
                       OptionRepository optionRepository, UserRepository userRepository,
                       QuestionTypeRepository questionTypeRepository, TeacherRepository teacherRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.userRepository = userRepository;
        this.questionTypeRepository = questionTypeRepository;
        this.teacherRepository = teacherRepository;
        
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
    
   
    public int getQuestionCountForQuiz(int quizId) {
        long count = questionRepository.countByQuizId(quizId);
        System.out.println("QuizService: Quiz ID " + quizId + " için soru sayısı: " + count);
        return (int) count; 
    }
    public void deactivateQuiz(int quizId, int actingUserId) {
        Optional<Quiz> optionalQuiz = quizRepository.findById(quizId);

        if (optionalQuiz.isEmpty()) {
            throw new QuizNotFoundException("Quiz bulunamadı ID: " + quizId+ "");
        }

        Quiz quiz = optionalQuiz.get();

 
        quiz.setActive(false);

        quizRepository.save(quiz);

        System.out.println("QuizService: Quiz başarıyla pasif hale getirildi - ID: " + quizId + " İşlemi yapan: " + actingUserId);
    }
   
    
    @Transactional(readOnly = true)
    public List<Question> getQuestionsByQuiz(int quizId, int viewerUserId) { 
        System.out.println("QuizService: Quize ait sorular getiriliyor - Quiz ID: " + quizId + ", Görüntüleyen Kullanıcı ID: " + viewerUserId);

         
         Quiz quiz = quizRepository.findById(quizId)
                 .orElseThrow(() -> {
                     System.err.println("QuizService: Sorulari getirilecek quiz bulunamadi - ID: " + quizId);
                     return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı."); 
                 });
         System.out.println("QuizService: Quiz bulundu - Ad: " + quiz.getName());

             
             User viewerUser = userRepository.findById(viewerUserId)
                     .orElseThrow(() -> {
                         System.err.println("QuizService: Sorulari görüntüleyen kullanıcı bulunamadi - ID: " + viewerUserId);
                         return new UserNotFoundException("ID " + viewerUserId + " olan kullanıcı bulunamadı."); 
                     });
             System.out.println("QuizService: Görüntüleyen kullanıcı bulundu - Kullanici Adi: " + viewerUser.getUsername());


             
             
             boolean isTeacher = quiz.getTeacher() != null && quiz.getTeacher().getId() == viewerUserId;
             boolean isAdmin = "ROLE_ADMIN".equals(viewerUser.getRole());

             if (!isAdmin && !isTeacher) {
                 System.err.println("QuizService: Kullanicinin bu quize ait sorulari görüntüleme yetkisi yok - Kullanici ID: " + viewerUserId + ", Quiz ID: " + quizId);
                 throw new UserNotAuthorizedException("Bu quize ait soruları görüntülemek için yetkiniz yok."); 
             }
             System.out.println("QuizService: Kullanici yetkisi dogrulandi.");

              
              System.out.println("DEBUG: Quiz ID: " + quiz.getId());
              System.out.println("DEBUG: Quiz name: " + quiz.getName());
              System.out.println("DEBUG: Quiz description: " + quiz.getDescription());
              System.out.println("DEBUG: Quiz isActive: " + quiz.isActive());
              
              
              List<Question> questions = questionRepository.findQuestionsWithOptionsByQuizId(quizId);
              
              if (!questions.isEmpty()) {
                  Question firstQuestion = questions.get(0);
                  System.out.println("DEBUG: First question ID: " + firstQuestion.getId());
                  System.out.println("DEBUG: First question text: " + firstQuestion.getQuestionSentence());
                  System.out.println("DEBUG: First question number: " + firstQuestion.getNumber());
                  
                  
                  if (firstQuestion.getOptions() != null) {
                      System.out.println("DEBUG: First question has " + firstQuestion.getOptions().size() + " options");
                  } else {
                      System.out.println("DEBUG: First question has no options loaded");
                  }
              }


              System.out.println("QuizService: Quiz ID " + quizId + " icin " + questions.size() + " adet soru getirildi.");

              
              return questions;
         }
 

    
    @Transactional
    public Quiz createQuiz(int teacherId, String name, String description, Integer durationMinutes, boolean isActive, String topic) {
        System.out.println("QuizService: Quiz olusturma başlatıldı - Ogretmen ID: " + teacherId + ", Ad: " + name);

        
        User user = userRepository.findById(teacherId)
                .orElseThrow(() -> {
                    System.err.println("QuizService: Kullanıcı bulunamadi - ID: " + teacherId);
                    return new UserNotFoundException("ID " + teacherId + " olan kullanıcı bulunamadı."); 
                });

        
        if (!"ROLE_TEACHER".equals(user.getRole())) {
            System.err.println("QuizService: Kullanici ogretmen yetkisine sahip degil - ID: " + teacherId + ", Rol: " + user.getRole());
            throw new IllegalArgumentException("ID " + teacherId + " olan kullanıcının quiz oluşturma yetkisi yok."); 
        }
        System.out.println("QuizService: Ogretmen bulundu ve yetkisi dogrulandi - Kullanici Adi: " + user.getUsername());

        
        Teacher teacher = null;
        
        if (teacherRepository == null) {
            System.err.println("CRITICAL ERROR: teacherRepository is null in createQuiz method!");
            System.out.println("Attempting to use userRepository as fallback...");
            
            
            User foundUser = userRepository.findById(teacherId)
                    .orElseThrow(() -> {
                        System.err.println("Fallback approach: User not found - ID: " + teacherId);
                        return new UserNotFoundException("ID " + teacherId + " olan kullanıcı bulunamadı.");
                    });
                    
            
            if (foundUser instanceof Teacher) {
                teacher = (Teacher) foundUser;
                System.out.println("Fallback approach: User is a Teacher instance, casting successful.");
            } else {
                System.err.println("Fallback approach: User is not a Teacher instance.");
                throw new UserNotAuthorizedException("Kullanıcı öğretmen değil. Quiz oluşturulamaz.");
            }
        } else {
            
            System.out.println("QuizService: Attempting normal approach with teacherRepository. teacherRepository is: " + (teacherRepository == null ? "null" : "not null"));
            try {
                if (teacherRepository == null) {
                    System.err.println("CRITICAL ERROR IMMINENT: teacherRepository is NULL right before findTeacherByUserId call!");
                }
                teacher = teacherRepository.findTeacherByUserId(teacherId) 
                    .orElseThrow(() -> {
                        System.err.println("QuizService: Teacher entity not found with User ID (normal approach) - User ID: " + teacherId);
                        return new UserNotFoundException("User ID " + teacherId + " için Teacher entity bulunamadı (normal yaklaşım).");
                    });
                System.out.println("QuizService: Successfully found Teacher with User ID (normal approach): " + teacherId);
            } catch (NullPointerException npe) {
                System.err.println("FATAL: NullPointerException directly caught when calling teacherRepository.findTeacherByUserId. teacherRepository IS NULL.");
                npe.printStackTrace(); 
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
                e.printStackTrace(); 
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

        
        Quiz newQuiz = new Quiz();
        newQuiz.setName(name);
        newQuiz.setTeacher(teacher); 
        newQuiz.setDescription(description);
        
        if (topic != null) {
            newQuiz.setTopic(topic);
            System.out.println("QuizService: Quiz konu ayarlandı - topic: " + topic);
        }
        if (durationMinutes != null) {
            newQuiz.setDuration(durationMinutes); 
        }
        
        newQuiz.setActive(isActive);
        System.out.println("QuizService: Quiz aktif durumu ayarlandı - isActive: " + isActive);
        

        Quiz createdQuiz = quizRepository.save(newQuiz);
        System.out.println("QuizService: Quiz başarıyla oluşturuldu - ID: " + createdQuiz.getId());

        return createdQuiz;
    }
    
    
    
    @Transactional
    public Quiz updateQuiz(int quizId, String name, String description, Integer durationMinutes, Boolean isActive, String topic, int updaterUserId) {
        System.out.println("QuizService: Quiz güncelleme başlatıldı - Quiz ID: " + quizId + ", Güncelleyen Kullanıcı ID: " + updaterUserId);

        Quiz quizToUpdate = quizRepository.findById(quizId)
        .orElseThrow(() -> {
            System.err.println("QuizService: Güncellenecek quiz bulunamadi - ID: " + quizId);
            return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı."); 
        });
        System.out.println("QuizService: Quiz bulundu - Ad: " + quizToUpdate.getName());

        User updaterUser = userRepository.findById(updaterUserId)
        .orElseThrow(() -> {
            System.err.println("QuizService: Güncelleyen kullanıcı bulunamadi - ID: " + updaterUserId);
            return new UserNotFoundException("ID " + updaterUserId + " olan kullanıcı bulunamadı."); 
        });
        System.out.println("QuizService: Güncelleyen kullanıcı bulundu - Kullanici Adi: " + updaterUser.getUsername());

        
        String userRole = updaterUser.getRole();
        boolean isAdmin = "ROLE_ADMIN".equals(userRole);
        boolean isTeacher = "ROLE_TEACHER".equals(userRole);
        
        
        boolean isQuizOwner = false;
        if (quizToUpdate.getTeacher() != null && isTeacher) {
            isQuizOwner = quizToUpdate.getTeacher().getId() == updaterUserId;
        }
        
        System.out.println("QuizService: Quiz güncelleme yetki kontrolü - Güncelleyen User ID: " + updaterUserId + 
                           ", Role: " + userRole + 
                           ", isAdmin: " + isAdmin + 
                           ", isTeacher: " + isTeacher +
                           ", isQuizOwner: " + isQuizOwner);

  
        if (!isAdmin && !isTeacher) {
            System.err.println("QuizService: Kullanicinin quizi güncelleme yetkisi yok - Kullanici ID: " + updaterUserId + ", Quiz ID: " + quizId);
            throw new UserNotAuthorizedException("Bu quizi güncellemek için yetkiniz yok. ADMIN veya TEACHER rolü gerekli."); 
        }
        
        System.out.println("QuizService: Kullanici yetkisi dogrulandi - isAdmin: " + isAdmin + ", isTeacher: " + isTeacher + ", isQuizOwner: " + isQuizOwner);


        
     if (name != null && !name.trim().isEmpty()) {
         quizToUpdate.setName(name.trim());
     }
     if (description != null && !description.trim().isEmpty()) {
         quizToUpdate.setDescription(description.trim());
     }
     
     if (topic != null) {
         quizToUpdate.setTopic(topic.trim());
         System.out.println("QuizService: Quiz konu güncellendi - topic: " + topic);
     }
     
     if (durationMinutes != null) {
         if (durationMinutes < 0) {
             throw new IllegalArgumentException("Süre negatif olamaz.");
         }
         quizToUpdate.setDuration(durationMinutes); 
     }
     
    
     if (isActive != null) {
         quizToUpdate.setActive(isActive);
     }

    


     Quiz updatedQuiz = quizRepository.save(quizToUpdate);
     System.out.println("QuizService: Quiz başarıyla güncellendi - ID: " + updatedQuiz.getId());

     return updatedQuiz;
     }

  
     @Transactional
     public void deleteQuiz(int quizId, int deleterUserId) { 
         System.out.println("QuizService: Quiz silme başlatıldı - Quiz ID: " + quizId + ", Silen Kullanıcı ID: " + deleterUserId);

         Quiz quizToDelete = quizRepository.findById(quizId)
         .orElseThrow(() -> {
             System.err.println("QuizService: Silinecek quiz bulunamadi - ID: " + quizId);
             return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı."); 
         });
         System.out.println("QuizService: Quiz bulundu - Ad: " + quizToDelete.getName());

         
         User deleterUser = userRepository.findById(deleterUserId)
         .orElseThrow(() -> {
             System.err.println("QuizService: Silen kullanıcı bulunamadi - ID: " + deleterUserId);
             return new UserNotFoundException("ID " + deleterUserId + " olan kullanıcı bulunamadı."); 
         });
         System.out.println("QuizService: Silen kullanıcı bulundu - Kullanici Adi: " + deleterUser.getUsername());


         
         boolean isTeacher = quizToDelete.getTeacher() != null && quizToDelete.getTeacher().getId() == deleterUserId;
         boolean isAdmin = "ROLE_ADMIN".equals(deleterUser.getRole());

         if (!isAdmin && !isTeacher) {
             System.err.println("QuizService: Kullanicinin quizi silme yetkisi yok - Kullanici ID: " + deleterUserId + ", Quiz ID: " + quizId);
             throw new UserNotAuthorizedException("Bu quizi silmek için yetkiniz yok."); 
         }
         System.out.println("QuizService: Kullanici yetkisi dogrulandi.");

         quizRepository.deleteById(quizId); 


         System.out.println("QuizService: Quiz başarıyla silindi - ID: " + quizId);

     }

  
    @Transactional(readOnly = true)
    public List<Quiz> getAllQuizzes() {
         System.out.println("QuizService: Tüm quizler getiriliyor.");
         List<Quiz> quizzes = quizRepository.findAll();

         System.out.println("QuizService: " + quizzes.size() + " adet quiz bulundu.");
         return quizzes;
     }

     @Transactional(readOnly = true)
    public Optional<Quiz> getQuizById(int quizId) {
         System.out.println("QuizService: Quiz getiriliyor - ID: " + quizId);
         Optional<Quiz> quizOptional = quizRepository.findById(quizId);

         if (quizOptional.isPresent()) {
             System.out.println("QuizService: Quiz bulundu - ID: " + quizId);
         } else {
              System.out.println("QuizService: Quiz bulunamadi - ID: " + quizId);
         }

         return quizOptional;
     }
     
     @Transactional(readOnly = true)
     public Optional<Quiz> getQuizById(int quizId, int userId) {
         System.out.println("QuizService: Quiz getiriliyor - Quiz ID: " + quizId + ", Kullanici ID: " + userId);
         
         Optional<Quiz> quizOptional = quizRepository.findById(quizId);
         
         if (!quizOptional.isPresent()) {
             System.out.println("QuizService: Quiz bulunamadi - ID: " + quizId);
             return Optional.empty();
         }
         
         User user = userRepository.findById(userId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Kullanici bulunamadi - ID: " + userId);
                 return new UserNotFoundException("Kullanıcı bulunamadı.");
             });
         System.out.println("QuizService: Kullanici bulundu - Kullanici Adı: " + user.getUsername() + ", Rol: " + user.getRole());
         
         Quiz quiz = quizOptional.get();
         boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
         boolean isTeacher = "ROLE_TEACHER".equals(user.getRole());
         boolean isStudent = "ROLE_STUDENT".equals(user.getRole());
         
         
         if (isAdmin) {
             System.out.println("QuizService: Admin yetkisi ile quiz görüntüleme izni verildi.");
             return quizOptional;
         }
         
         if (isTeacher) {
             Teacher quizTeacher = quiz.getTeacher();
             if (quizTeacher != null) {
                 System.out.println("QuizService: Quiz'in öğretmeni: " + quizTeacher.getId() + ", mevcut öğretmen: " + userId);
                 
                 if (quizTeacher.getId() == userId) {
                     System.out.println("QuizService: Öğretmenin kendi quizi, görüntüleme izni verildi.");
                     return quizOptional;
                 }
                 
                 System.out.println("QuizService: Başka bir öğretmenin quizi, öğretmen rolü ile görüntüleme izni verildi.");
                 return quizOptional;
             }
         }
         
         
         if (isStudent && quiz.isActive()) {
             System.out.println("QuizService: Öğrenci aktif quizi görüntüleme izni verildi.");
             return quizOptional;
         }
         
         System.err.println("QuizService: Kullanıcının bu quizi görüntüleme yetkisi yok - Kullanıcı ID: " + userId + ", Quiz ID: " + quizId);
         throw new UserNotAuthorizedException("Bu quizi görüntülemek için yetkiniz yok.");
     }

    public List<Quiz> getQuizzesByTeacher(int teacherId) {
        System.out.println("QuizService: Öğretmenin AKTİF quizleri getiriliyor - Öğretmen ID: " + teacherId);
        
        try {
            Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> {
                    System.err.println("QuizService: Öğretmen (entity) bulunamadı - ID: " + teacherId);
                    return new UserNotFoundException("ID " + teacherId + " olan öğretmen bulunamadı.");
                });
            
            List<Quiz> activeQuizzes = quizRepository.findActiveQuizzesByTeacher(teacher);
            System.out.println("QuizService: " + activeQuizzes.size() + " adet aktif quiz bulundu");
            return activeQuizzes;
            
        } catch (Exception e) {
            System.err.println("QuizService: Öğretmen quizleri getirilirken hata oluştu - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    
    @Transactional(readOnly = true)
    public List<Quiz> getAllQuizzesByTeacher(int teacherId) {
        System.out.println("QuizService: Öğretmenin TÜM quizleri getiriliyor - Öğretmen ID: " + teacherId);
        
        try {
            Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> {
                    System.err.println("QuizService: Öğretmen (entity) bulunamadı - ID: " + teacherId);
                    return new UserNotFoundException("ID " + teacherId + " olan öğretmen bulunamadı.");
                });
            
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
    
    Quiz quiz = quizRepository.findById(quizId)
        .orElseThrow(() -> new QuizNotFoundException("Quiz not found with ID: " + quizId));
    
    QuestionType questionType = questionTypeRepository.findById(questionTypeId)
        .orElseThrow(() -> new QuestionTypeNotFoundException("Question type not found with ID: " + questionTypeId));
    
    User user = userRepository.findById(adderUserId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + adderUserId));
    
    boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
    boolean isTeacher = false;
    
    if ("ROLE_TEACHER".equals(user.getRole()) && quiz.getTeacher() != null) {
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
    
    Question question = new Question();
    question.setNumber(number);
    question.setQuestionSentence(questionSentence);
    question.setType(questionType);
    question.setQuiz(quiz);  
    
    if (points > 0) {
        System.out.println("QuizService: Soru puanı ayarlanıyor - Puan: " + points);
        question.setPoints(points);
    } else {
        question.setPoints(1); 
        System.out.println("QuizService: Varsayılan soru puanı ayarlandı - Puan: 1");
    }
    
if (correctAnswerText != null && !correctAnswerText.trim().isEmpty() && 
!"Çoktan Seçmeli".equalsIgnoreCase(questionType.getTypeName())) {
QuestionAnswer answer = new QuestionAnswer();
answer.setAnswer(correctAnswerText);
answer.setCorrect(true);
answer.setQuestion(question);

answer.setTakeQuiz(null);

question.setAnswer(answer);
}
    
    if (options != null && !options.isEmpty()) {
        for (Option option : options) {
            option.setQuestion(question);
            question.getOptions().add(option);
        }
    }
    
    Question savedQuestion = questionRepository.save(question);
    
    quiz.getQuestions().add(savedQuestion);
    quizRepository.save(quiz);  
    
    return savedQuestion;
}

     @Transactional 
     public void removeQuestionFromQuiz(int quizId, int questionId, int removerUserId) {
         System.out.println("QuizService: Quizden soru silme başlatıldı - Quiz ID: " + quizId + ", Soru ID: " + questionId + ", Silen Kullanıcı ID: " + removerUserId);

         Quiz quiz = quizRepository.findById(quizId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Soru silinecek quiz bulunamadi - ID: " + quizId);
                 return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı."); 
             });
         System.out.println("QuizService: Quiz bulundu - Ad: " + quiz.getName());

         Question questionToRemove = questionRepository.findById(questionId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Silinecek soru bulunamadi - ID: " + questionId);
                 return new QuestionNotFoundException("ID " + questionId + " olan soru bulunamadı."); 
             });
         System.out.println("QuizService: Soru bulundu - ID: " + questionToRemove.getId());

         User removerUser = userRepository.findById(removerUserId)
             .orElseThrow(() -> {
                 System.err.println("QuizService: Soru silen kullanıcı bulunamadi - ID: " + removerUserId);
                 return new UserNotFoundException("ID " + removerUserId + " olan kullanıcı bulunamadı."); 
             });
         System.out.println("QuizService: Silen kullanıcı bulundu - Kullanici Adi: " + removerUser.getUsername());


         boolean isTeacher = quiz.getTeacher() != null && quiz.getTeacher().getId() == removerUserId;
         boolean isAdmin = "ROLE_ADMIN".equals(removerUser.getRole());

         if (!isAdmin && !isTeacher) {
             System.err.println("QuizService: Kullanicinin quizden soru silme yetkisi yok - Kullanici ID: " + removerUserId + ", Quiz ID: " + quizId);
             throw new UserNotAuthorizedException("Bu quizden soru silmek için yetkiniz yok."); 
         }
         System.out.println("QuizService: Kullanici yetkisi dogrulandi.");

         if (questionToRemove.getQuiz().getId() != quizId) {
             System.err.println("QuizService: Soru bu quize ait degil - Soru ID: " + questionId + ", Quiz ID: " + quizId);
             throw new QuestionDoesNotBelongToQuizException("ID " + questionId + " olan soru ID " + quizId + " olan quize ait değil."); 
         }
         System.out.println("QuizService: Soru, quize ait olduğu doğrulandı.");


         quiz.removeQuestion(questionToRemove); 

         quizRepository.save(quiz);

         System.out.println("QuizService: Soru ID " + questionId + " başarıyla quiz ID " + quizId + " den silindi.");

     }

  
     @Transactional 
     public Question updateQuestionInQuiz(int quizId, int questionId, Integer number, String questionSentence, String correctAnswerText, int questionTypeId, int points, List<Option> options, int updaterUserId) {
         System.out.println("QuizService: Quizdeki soru güncelleniyor - Quiz ID: " + quizId + ", Soru ID: " + questionId + ", Güncelleyen Kullanıcı ID: " + updaterUserId);

         Quiz quiz = quizRepository.findById(quizId)
         .orElseThrow(() -> {
             System.err.println("QuizService: Soru güncellenecek quiz bulunamadi - ID: " + quizId);
             return new QuizNotFoundException("ID " + quizId + " olan quiz bulunamadı."); 
         });
        System.out.println("QuizService: Quiz bulundu - Ad: " + quiz.getName());


        Question questionToUpdate = questionRepository.findById(questionId)
        .orElseThrow(() -> {
            System.err.println("QuizService: Güncellenecek soru bulunamadi - ID: " + questionId);
        return new QuestionNotFoundException("ID " + questionId + " olan soru bulunamadı."); 
        });
        System.out.println("QuizService: Soru bulundu - ID: " + questionToUpdate.getId());

        if (questionToUpdate.getQuiz().getId() != quizId) {
            System.err.println("QuizService: Soru bu quize ait degil - Soru ID: " + questionId + ", Quiz ID: " + quizId);
            throw new QuestionDoesNotBelongToQuizException("ID " + questionId + " olan soru ID " + quizId + " olan quize ait değil."); 
        }
        System.out.println("QuizService: Soru, quize ait olduğu doğrulandı.");


        User updaterUser = userRepository.findById(updaterUserId)
        .orElseThrow(() -> {
            System.err.println("QuizService: Güncelleyen kullanıcı bulunamadi - ID: " + updaterUserId);
            return new UserNotFoundException("ID " + updaterUserId + " olan kullanıcı bulunamadı."); 
        });
    System.out.println("QuizService: Güncelleyen kullanıcı bulundu - Kullanici Adi: " + updaterUser.getUsername());

    String userRole = updaterUser.getRole();
    boolean isAdmin = "ROLE_ADMIN".equals(userRole);
    boolean isTeacher = "ROLE_TEACHER".equals(userRole);
    
    boolean isQuizOwner = false;
    if (quiz.getTeacher() != null && isTeacher) {
        isQuizOwner = quiz.getTeacher().getId() == updaterUserId;
    }
    
    System.out.println("QuizService: Soru güncelleme yetki kontrolü - Güncelleyen User ID: " + updaterUserId + 
                    ", Role: " + userRole + 
                    ", isAdmin: " + isAdmin + 
                    ", isTeacher: " + isTeacher + 
                    ", isQuizOwner: " + isQuizOwner);

    if (!isAdmin && !isTeacher) {
        System.err.println("QuizService: Kullanicinin quizdeki soruyu güncelleme yetkisi yok - Kullanici ID: " + updaterUserId + ", Quiz ID: " + quizId);
        throw new UserNotAuthorizedException("Bu quizdeki soruyu güncellemek için yetkiniz yok. ADMIN veya TEACHER rolü gerekli.");
    }
    System.out.println("QuizService: Kullanici yetkisi dogrulandi.");

    QuestionType questionType = null;
    if (questionTypeId != 0) {
        questionType = questionTypeRepository.findById(questionTypeId)
            .orElseThrow(() -> {
                System.err.println("QuizService: Güncellenecek soru tipi bulunamadi - ID: " + questionTypeId);
                return new QuestionTypeNotFoundException("ID " + questionTypeId + " olan soru tipi bulunamadı.");
            });
        System.out.println("QuizService: Güncellenecek soru tipi bulundu - Ad: " + questionType.getTypeName());
    }

    questionToUpdate.setNumber(number);
    questionToUpdate.setQuestionSentence(questionSentence);
    if (questionType != null) {
        questionToUpdate.setType(questionType);
    }
    
    if (points > 0) {
        System.out.println("QuizService: Soru puanı ayarlanıyor - Puan: " + points);
        questionToUpdate.setPoints(points);
    } else {
        System.out.println("QuizService: Soru puanı değiştirilmedi, mevcut puan: " + questionToUpdate.getPoints());
    }

    String questionTypeName = questionType != null ? questionType.getTypeName() : questionToUpdate.getType().getTypeName();
    boolean isMultipleChoice = "Çoktan Seçmeli".equalsIgnoreCase(questionTypeName);
    
    System.out.println("QuizService: Soru tipi: " + questionTypeName + ", isMultipleChoice: " + isMultipleChoice);
    
    if (isMultipleChoice) {
        
        System.out.println("QuizService: Çoktan seçmeli soru güncelleniyor");
        
        if (questionToUpdate.getAnswer() != null) {
            questionToUpdate.setAnswer(null);
        }
        
        
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
        
        System.out.println("QuizService: Çoktan seçmeli olmayan soru güncelleniyor");
        
        if (correctAnswerText != null && !correctAnswerText.isEmpty()) {
            System.out.println("QuizService: Doğru cevap güncelleniyor - Cevap: " + correctAnswerText);
            QuestionAnswer currentAnswer = questionToUpdate.getAnswer();
            if (currentAnswer == null) {
                currentAnswer = new QuestionAnswer();
                currentAnswer.setQuestion(questionToUpdate); 
            }
            currentAnswer.setAnswer(correctAnswerText);
            currentAnswer.setCorrect(true); 
            questionToUpdate.setAnswer(currentAnswer); 
        } else {
            System.out.println("QuizService: UYARI - Çoktan seçmeli olmayan soru için doğru cevap bulunamadı!");
        }
        
        
        if (questionToUpdate.getOptions() != null && !questionToUpdate.getOptions().isEmpty()) {
            System.out.println("QuizService: Çoktan seçmeli olmayan soru için seçenekler temizleniyor");
            new ArrayList<>(questionToUpdate.getOptions()).forEach(questionToUpdate::removeOption);
        }
    }

        
        Question savedQuestion = questionRepository.save(questionToUpdate);
        System.out.println("QuizService: Soru başarıyla güncellendi - ID: " + savedQuestion.getId());

        return savedQuestion;
    } 

 
         @Transactional 
         public Option addOptionToQuestion(int questionId, String text, boolean isCorrect, int adderUserId) {
             System.out.println("QuizService: Soruya şık ekleme başlatıldı - Soru ID: " + questionId + ", Ekleyen Kullanıcı ID: " + adderUserId);

             
             Question question = questionRepository.findById(questionId)
                     .orElseThrow(() -> {
                         System.err.println("QuizService: Sik eklenecek soru bulunamadi - ID: " + questionId);
                         return new QuestionNotFoundException("ID " + questionId + " olan soru bulunamadı."); 
                     });
             System.out.println("QuizService: Soru bulundu - ID: " + question.getId());


            
            User adderUser = userRepository.findById(adderUserId)
            .orElseThrow(() -> {
                System.err.println("QuizService: Sik ekleyen kullanıcı bulunamadi - ID: " + adderUserId);
                return new UserNotFoundException("ID " + adderUserId + " olan kullanıcı bulunamadı."); 
            });
            System.out.println("QuizService: Sik ekleyen kullanıcı bulundu - Kullanici Adi: " + adderUser.getUsername());

            
            Quiz quizOfQuestion = question.getQuiz();
            if (quizOfQuestion == null) {
                
                System.err.println("QuizService: Soru bağlı olduğu quizi bulamadi - Soru ID: " + questionId);
                throw new IllegalStateException("ID " + questionId + " olan soru herhangi bir quize bağlı değil."); 
            }

            boolean isTeacher = quizOfQuestion.getTeacher() != null && quizOfQuestion.getTeacher().getId() == adderUserId;
            boolean isAdmin = "ROLE_ADMIN".equals(adderUser.getRole());

            if (!isAdmin && !isTeacher) {
                System.err.println("QuizService: Kullanicinin bu soruya sik ekleme yetkisi yok - Kullanici ID: " + adderUserId + ", Soru ID: " + questionId);
                throw new UserNotAuthorizedException("Bu soruya şık eklemek için yetkiniz yok."); 
            }
            System.out.println("QuizService: Kullanici yetkisi dogrulandi.");


            
            boolean isMultipleChoice = question.getType() != null && "Çoktan Seçmeli".equals(question.getType().getTypeName());

            if (!isMultipleChoice) {
                System.err.println("QuizService: Soru çoktan seçmeli degil, şık eklenemez - Soru ID: " + questionId + ", Tip: " + (question.getType() != null ? question.getType().getTypeName() : "Bilinmiyor"));
                throw new InvalidQuestionTypeForOptionException("ID " + questionId + " olan soru çoktan seçmeli değil, şık eklenemez."); 
            }
            System.out.println("QuizService: Soru tipi çoktan seçmeli olduğu doğrulandı.");


            
            Option newOption = Option.createOption(question, text, isCorrect); 


            
            
            
            question.addOption(newOption); 


            
            Question savedQuestion = questionRepository.save(question);
            System.out.println("QuizService: Soru ID " + savedQuestion.getId() + " başarıyla güncellendi. Yeni Şık ID: " + newOption.getId());

            
            return newOption;
        }

      
         @Transactional 
             
             public void deleteOption(int optionId, int deleterUserId) {
                 System.out.println("QuizService: Şık silme başlatıldı - Şık ID: " + optionId + ", Silen Kullanıcı ID: " + deleterUserId);

                 
                 Option optionToDelete = optionRepository.findById(optionId)
                         .orElseThrow(() -> {
                             System.err.println("QuizService: Silinecek sik bulunamadi - ID: " + optionId);
                             return new OptionNotFoundException("ID " + optionId + " olan şık bulunamadı."); 
                         });
                 System.out.println("QuizService: Şık bulundu - ID: " + optionToDelete.getId());

             
             User deleterUser = userRepository.findById(deleterUserId)
                     .orElseThrow(() -> {
                         System.err.println("QuizService: Sik silen kullanıcı bulunamadi - ID: " + deleterUserId);
                         return new UserNotFoundException("ID " + deleterUserId + " olan kullanıcı bulunamadı."); 
                     });
             System.out.println("QuizService: Silen kullanıcı bulundu - Kullanici Adi: " + deleterUser.getUsername());

                
                Question questionOfOption = optionToDelete.getQuestion();
                if (questionOfOption == null) {
                    
                    throw new IllegalStateException("ID " + optionId + " olan şık herhangi bir soruya bağlı değil."); 
                }
                Quiz quizOfQuestion = questionOfOption.getQuiz();
                if (quizOfQuestion == null) {
                    
                    System.err.println("QuizService: Şıkkın sorusu bağlı olduğu quizi bulamadi - Şık ID: " + optionId + ", Soru ID: " + questionOfOption.getId());
                    throw new IllegalStateException("ID " + questionOfOption.getId() + " olan soru herhangi bir quize bağlı değil."); 
                }


                 boolean isTeacher = quizOfQuestion.getTeacher() != null && quizOfQuestion.getTeacher().getId() == deleterUserId;
                 boolean isAdmin = "ROLE_ADMIN".equals(deleterUser.getRole());

                 if (!isAdmin && !isTeacher) {
                     System.err.println("QuizService: Kullanicinin bu şıkkı silme yetkisi yok - Kullanici ID: " + deleterUserId + ", Şık ID: " + optionId);
                     throw new UserNotAuthorizedException("Bu şıkkı silmek için yetkiniz yok."); 
                 }
                 System.out.println("QuizService: Kullanici yetkisi dogrulandi.");


                 
                 optionRepository.deleteById(optionId); 

                 System.out.println("QuizService: Şık ID " + optionId + " başarıyla silindi.");

             }

      
         @Transactional 
             
             public Option updateOption(int optionId, String text, boolean isCorrect, int updaterUserId) {
                 System.out.println("QuizService: Şık güncelleme başlatıldı - Şık ID: " + optionId + ", Güncelleyen Kullanıcı ID: " + updaterUserId);

                 
                 Option optionToUpdate = optionRepository.findById(optionId)
                         .orElseThrow(() -> {
                             System.err.println("QuizService: Güncellenecek sik bulunamadi - ID: " + optionId);
                             return new OptionNotFoundException("ID " + optionId + " olan şık bulunamadı."); 
                         });
                 System.out.println("QuizService: Şık bulundu - ID: " + optionToUpdate.getId());


                 
                 User updaterUser = userRepository.findById(updaterUserId)
                         .orElseThrow(() -> {
                             System.err.println("QuizService: Şık güncelleyen kullanıcı bulunamadi - ID: " + updaterUserId);
                             return new UserNotFoundException("ID " + updaterUserId + " olan kullanıcı bulunamadı."); 
                         });
                 System.out.println("QuizService: Şık güncelleyen kullanıcı bulundu - Kullanici Adi: " + updaterUser.getUsername());

                
                Question questionOfOption = optionToUpdate.getQuestion();
                if (questionOfOption == null) {
                    
                    throw new IllegalStateException("ID " + optionId + " olan şık herhangi bir soruya bağlı değil."); 
                }
                Quiz quizOfQuestion = questionOfOption.getQuiz();
                if (quizOfQuestion == null) {
                
                    System.err.println("QuizService: Şıkkın sorusu bağlı olduğu quizi bulamadi - Şık ID: " + optionId + ", Soru ID: " + questionOfOption.getId());
                    throw new IllegalStateException("ID " + questionOfOption.getId() + " olan soru herhangi bir quize bağlı değil."); 
                }

                 boolean isTeacher = quizOfQuestion.getTeacher() != null && quizOfQuestion.getTeacher().getId() == updaterUserId;
                 boolean isAdmin = "ROLE_ADMIN".equals(updaterUser.getRole());

                 if (!isAdmin && !isTeacher) {
                     System.err.println("QuizService: Kullanicinin bu şıkkı güncelleme yetkisi yok - Kullanici ID: " + updaterUserId + ", Şık ID: " + optionId);
                     throw new UserNotAuthorizedException("Bu şıkkı güncellemek için yetkiniz yok."); 
                 }
                 System.out.println("QuizService: Kullanici yetkisi dogrulandi.");


                 
                 if (text != null && !text.trim().isEmpty()) {
                     optionToUpdate.setText(text.trim());
                 }
                 
                 optionToUpdate.setCorrect(isCorrect);


                 
                 Option updatedOption = optionRepository.save(optionToUpdate);

                 System.out.println("QuizService: Şık ID " + updatedOption.getId() + " başarıyla güncellendi.");
                 return updatedOption;
             }


     
    @Transactional
    public int activateAllQuizzesForTeacher(int teacherId) {
        System.out.println("QuizService: Activating all quizzes for teacher ID: " + teacherId);
        
        List<Quiz> teacherQuizzes = this.getAllQuizzesByTeacher(teacherId);
        
        if (teacherQuizzes.isEmpty()) {
            System.out.println("QuizService: No quizzes found for teacher ID: " + teacherId);
            return 0;
        }
        
        
        int totalQuizzes = teacherQuizzes.size();
        System.out.println("QuizService: Found " + totalQuizzes + " quizzes to activate");
        
        
        teacherQuizzes.forEach(quiz -> {
            if (!quiz.isActive()) {
                quiz.setActive(true);
                quiz = quizRepository.save(quiz);
                System.out.println("QuizService: Activated quiz ID: " + quiz.getId() + ", Name: " + quiz.getName());
            }
        });
        
        quizRepository.flush();
        
        System.out.println("QuizService: Successfully activated all " + totalQuizzes + " quizzes for teacher ID: " + teacherId);
        return totalQuizzes;
    }

}