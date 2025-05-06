-- MySQL Database Schema for Quiz System
-- Based on the provided UML class diagram

-- Drop database if exists (be careful with this in production!)
DROP DATABASE IF EXISTS quiz_system;

-- Create database
CREATE DATABASE quiz_system;
USE quiz_system;

-- Create QuestionType table
CREATE TABLE QuestionType (
    id INT PRIMARY KEY AUTO_INCREMENT,
    typeName VARCHAR(50) NOT NULL
);

-- Create User table as base table
CREATE TABLE User (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    age INT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    createDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updateDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    userName VARCHAR(50) NOT NULL UNIQUE,
    isActive BOOLEAN NOT NULL DEFAULT TRUE,
    userType ENUM('Admin', 'Teacher', 'Student') NOT NULL
);

-- Create Admin table
CREATE TABLE Admin (
    userId INT PRIMARY KEY,
    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE
);

-- Create Teacher table
CREATE TABLE Teacher (
    userId INT PRIMARY KEY,
    subject VARCHAR(100),
    graduateSchool VARCHAR(100),
    diplomaNumber VARCHAR(50),
    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE
);

-- Create Student table
CREATE TABLE Student (
    userId INT PRIMARY KEY,
    schoolName VARCHAR(100),
    studentId VARCHAR(50),
    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE
);

-- Create Announcement table
CREATE TABLE Announcement (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    publisherId INT NOT NULL,
    FOREIGN KEY (publisherId) REFERENCES User(id) ON DELETE CASCADE
);

-- Create Quiz table
CREATE TABLE Quiz (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    duration INT,
    teacherId INT NOT NULL,
    startDate DATETIME NOT NULL,
    endDate DATETIME NOT NULL,
    isActive BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (teacherId) REFERENCES Teacher(userId) ON DELETE CASCADE
);

-- Create Question table
CREATE TABLE Question (
    id INT PRIMARY KEY AUTO_INCREMENT,
    number INT NOT NULL,
    quizId INT NOT NULL,
    questionSentence TEXT NOT NULL,
    typeId INT NOT NULL,
    FOREIGN KEY (quizId) REFERENCES Quiz(id) ON DELETE CASCADE,
    FOREIGN KEY (typeId) REFERENCES QuestionType(id) ON DELETE RESTRICT,
    UNIQUE KEY (quizId, number)
);

-- Create TestQuestion-specific Options table
CREATE TABLE Options (
    id INT PRIMARY KEY AUTO_INCREMENT,
    questionId INT NOT NULL,
    text TEXT NOT NULL,
    isCorrect BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (questionId) REFERENCES Question(id) ON DELETE CASCADE
);

-- Create TakeQuiz table
CREATE TABLE TakeQuiz (
    id INT PRIMARY KEY AUTO_INCREMENT,
    studentId INT NOT NULL,
    quizId INT NOT NULL,
    startTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    endTime DATETIME,
    FOREIGN KEY (studentId) REFERENCES Student(userId) ON DELETE CASCADE,
    FOREIGN KEY (quizId) REFERENCES Quiz(id) ON DELETE CASCADE
);

-- Create QuestionAnswer table
CREATE TABLE QuestionAnswer (
    id INT PRIMARY KEY AUTO_INCREMENT,
    takeQuizId INT NOT NULL,
    questionId INT NOT NULL,
    answer TEXT,
    isCorrect BOOLEAN,
    FOREIGN KEY (takeQuizId) REFERENCES TakeQuiz(id) ON DELETE CASCADE,
    FOREIGN KEY (questionId) REFERENCES Question(id) ON DELETE CASCADE
);

-- Create AuthenticationService table
CREATE TABLE AuthenticationService (
    sessionId VARCHAR(255) PRIMARY KEY,
    userId INT NOT NULL,
    lastLoginDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    isActive BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE
);

-- Create MailService table (if needed to store mail configuration)
CREATE TABLE MailService (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_mail_address VARCHAR(100) NOT NULL,
    sender_password VARCHAR(255) NOT NULL
);

-- Create User-Announcement relationship table
CREATE TABLE UserAnnouncement (
    userId INT NOT NULL,
    announcementId INT NOT NULL,
    PRIMARY KEY (userId, announcementId),
    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (announcementId) REFERENCES Announcement(id) ON DELETE CASCADE
);

-- Create indices for performance optimization
CREATE INDEX idx_quiz_teacher ON Quiz(teacherId);
CREATE INDEX idx_question_quiz ON Question(quizId);
CREATE INDEX idx_takequiz_student ON TakeQuiz(studentId);
CREATE INDEX idx_takequiz_quiz ON TakeQuiz(quizId);
CREATE INDEX idx_questionanswer_takequiz ON QuestionAnswer(takeQuizId);
CREATE INDEX idx_questionanswer_question ON QuestionAnswer(questionId);

-- Insert default question types
INSERT INTO QuestionType (typeName) VALUES ('TestQuestion'), ('OpenEndedQuestion');

-- Stored procedure to create a new user
DELIMITER //
CREATE PROCEDURE CreateUser(
    IN p_name VARCHAR(100),
    IN p_surname VARCHAR(100),
    IN p_age INT,
    IN p_email VARCHAR(100),
    IN p_password VARCHAR(255),
    IN p_userName VARCHAR(50),
    IN p_userType ENUM('Admin', 'Teacher', 'Student'),
    OUT p_userId INT
)
BEGIN
    INSERT INTO User (name, surname, age, email, password, userName, userType)
    VALUES (p_name, p_surname, p_age, p_email, p_password, p_userName, p_userType);
    
    SET p_userId = LAST_INSERT_ID();
    
    CASE p_userType
        WHEN 'Admin' THEN
            INSERT INTO Admin (userId) VALUES (p_userId);
        WHEN 'Teacher' THEN
            INSERT INTO Teacher (userId) VALUES (p_userId);
        WHEN 'Student' THEN
            INSERT INTO Student (userId) VALUES (p_userId);
    END CASE;
END //
DELIMITER ;

-- Stored procedure to authenticate user
DELIMITER //
CREATE PROCEDURE AuthenticateUser(
    IN p_email VARCHAR(100),
    IN p_password VARCHAR(255),
    OUT p_sessionId VARCHAR(255)
)
BEGIN
    DECLARE v_userId INT;
    DECLARE v_isActive BOOLEAN;
    
    SELECT id, isActive INTO v_userId, v_isActive
    FROM User
    WHERE email = p_email AND password = p_password;
    
    IF v_userId IS NOT NULL AND v_isActive = TRUE THEN
        SET p_sessionId = UUID();
        
        INSERT INTO AuthenticationService (sessionId, userId)
        VALUES (p_sessionId, v_userId);
    ELSE
        SET p_sessionId = NULL;
    END IF;
END //
DELIMITER ;

-- Stored procedure to create a quiz
DELIMITER //
CREATE PROCEDURE CreateQuiz(
    IN p_name VARCHAR(200),
    IN p_teacherId INT,
    IN p_startDate DATETIME,
    IN p_endDate DATETIME,
    IN p_duration INT,
    OUT p_quizId INT
)
BEGIN
    INSERT INTO Quiz (name, teacherId, startDate, endDate, duration)
    VALUES (p_name, p_teacherId, p_startDate, p_endDate, p_duration);
    
    SET p_quizId = LAST_INSERT_ID();
END //
DELIMITER ;

-- Trigger to ensure the teacher exists before creating a quiz
DELIMITER //
CREATE TRIGGER before_quiz_insert
BEFORE INSERT ON Quiz
FOR EACH ROW
BEGIN
    DECLARE teacher_exists INT;
    
    SELECT COUNT(*) INTO teacher_exists
    FROM Teacher
    WHERE userId = NEW.teacherId;
    
    IF teacher_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot create quiz: Teacher does not exist';
    END IF;
END //
DELIMITER ;

-- Trigger to validate quiz dates
DELIMITER //
CREATE TRIGGER validate_quiz_dates
BEFORE INSERT ON Quiz
FOR EACH ROW
BEGIN
    IF NEW.startDate >= NEW.endDate THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'End date must be after start date';
    END IF;
END //
DELIMITER ;

-- Trigger to update User updateDate when related tables are modified
DELIMITER //
CREATE TRIGGER after_student_update
AFTER UPDATE ON Student
FOR EACH ROW
BEGIN
    UPDATE User SET updateDate = CURRENT_TIMESTAMP WHERE id = NEW.userId;
END //

CREATE TRIGGER after_teacher_update
AFTER UPDATE ON Teacher
FOR EACH ROW
BEGIN
    UPDATE User SET updateDate = CURRENT_TIMESTAMP WHERE id = NEW.userId;
END //
DELIMITER ;

-- View for showing student quiz results
CREATE VIEW StudentQuizResults AS
SELECT 
    s.userId AS studentId,
    CONCAT(u.name, ' ', u.surname) AS studentName,
    q.id AS quizId,
    q.name AS quizName,
    tq.id AS takeQuizId,
    tq.startTime,
    tq.endTime,
    TIMESTAMPDIFF(MINUTE, tq.startTime, IFNULL(tq.endTime, NOW())) AS timeTaken,
    COUNT(qa.id) AS questionsAnswered,
    SUM(IF(qa.isCorrect = TRUE, 1, 0)) AS correctAnswers,
    ROUND((SUM(IF(qa.isCorrect = TRUE, 1, 0)) / COUNT(qa.id)) * 100, 2) AS score
FROM 
    Student s
    JOIN User u ON s.userId = u.id
    JOIN TakeQuiz tq ON s.userId = tq.studentId
    JOIN Quiz q ON tq.quizId = q.id
    LEFT JOIN QuestionAnswer qa ON tq.id = qa.takeQuizId
GROUP BY 
    s.userId, q.id, tq.id;

-- View for showing teacher's quizzes statistics
CREATE VIEW TeacherQuizStatistics AS
SELECT 
    t.userId AS teacherId,
    CONCAT(u.name, ' ', u.surname) AS teacherName,
    q.id AS quizId,
    q.name AS quizName,
    COUNT(DISTINCT tq.id) AS attempts,
    AVG(ROUND((SUM(IF(qa.isCorrect = TRUE, 1, 0)) / COUNT(qa.id)) * 100, 2)) 
        OVER (PARTITION BY q.id) AS averageScore,
    MIN(ROUND((SUM(IF(qa.isCorrect = TRUE, 1, 0)) / COUNT(qa.id)) * 100, 2)) 
        OVER (PARTITION BY q.id) AS minScore,
    MAX(ROUND((SUM(IF(qa.isCorrect = TRUE, 1, 0)) / COUNT(qa.id)) * 100, 2)) 
        OVER (PARTITION BY q.id) AS maxScore
FROM 
    Teacher t
    JOIN User u ON t.userId = u.id
    JOIN Quiz q ON t.userId = q.teacherId
    LEFT JOIN TakeQuiz tq ON q.id = tq.quizId
    LEFT JOIN QuestionAnswer qa ON tq.id = qa.takeQuizId
GROUP BY 
    t.userId, q.id, tq.id;