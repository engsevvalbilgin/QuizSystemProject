package com.example.QuizSystemProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            UserNotFoundException.class,
            QuizNotFoundException.class,
            QuestionNotFoundException.class,
            OptionNotFoundException.class,
            QuestionTypeNotFoundException.class,
            QuizSessionNotFoundException.class

    })

    public ResponseEntity<String> handleResourceNotFoundException(RuntimeException ex) {
        System.err.println("GlobalExceptionHandler: Kaynak bulunamadi (404) hatasi yakalandi - " + ex.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
            status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        }
        return ResponseEntity.status(status).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<String> handleUserNotAuthorizedException(UserNotAuthorizedException ex) {
        System.err.println("GlobalExceptionHandler: Yetkilendirme (403) hatasi yakalandi - " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            QuestionDoesNotBelongToQuizException.class,
            InvalidQuestionTypeForOptionException.class,
            QuizNotAvailableException.class,
            InvalidQuestionTypeForAnswerException.class,
            QuizSessionExpiredException.class,
            InvalidOptionForQuestionException.class
    })
    public ResponseEntity<String> handleBadRequestException(RuntimeException ex) {
        System.err.println("GlobalExceptionHandler: Geçersiz İstek (400) hatasi yakalandi - " + ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
            status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        }
        return ResponseEntity.status(status).body(ex.getMessage());
    }

    @ExceptionHandler({
            DuplicateUsernameException.class,
            DuplicateEmailException.class,
            AnswerAlreadySubmittedException.class,
            QuizAlreadyTakenException.class,
            QuizSessionAlreadyCompletedException.class
    })
    public ResponseEntity<String> handleConflictException(RuntimeException ex) {
        System.err.println("GlobalExceptionHandler: Veri cakismasi (409) hatasi yakalandi - " + ex.getMessage());
        HttpStatus status = HttpStatus.CONFLICT;
        if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
            status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        }
        return ResponseEntity.status(status).body(ex.getMessage());
    }

    @ExceptionHandler({
            InvalidTokenException.class,
            ExpiredTokenException.class
    })
    public ResponseEntity<String> handleTokenException(RuntimeException ex) {
        System.err.println("GlobalExceptionHandler: Token hatasi yakalandi - " + ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
            status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        }
        return ResponseEntity.status(status).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleGenericRuntimeException(RuntimeException ex) {
        System.err.println("GlobalExceptionHandler: Beklenmeyen RuntimeException yakalandi - " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Beklenmeyen sunucu hatasi olustu.");
    }

}