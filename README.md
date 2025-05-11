### **Online Quiz Platform - README**

---

#### **Project Description**

This project is an Online Quiz Platform developed using Java Swing and MySQL. Users (students) can create quizzes and exams on the system, participate in them, and automatically receive scores. Teachers can create quizzes and exams, assign users, and track results.

#### **Features**
- **User Authentication**: Users can log in with email and password. New users can register.
- **Email Verification**: New users can verify their email after registering to activate their account.
- **Quiz and Exam Creation**: Teachers can create quizzes with multiple-choice and open-ended questions.
- **Automatic Grading**: An automatic grading system for multiple-choice questions.
- **Email Notifications**: Students are notified via email about assigned quizzes, exams, and results.
- **Results Tracking**: Students can view their quiz results, while teachers can manage the results.

---

#### **Technologies and Tools**
- **Java**: For the application interface.
- **Swing**: For developing the Graphical User Interface (GUI) with Java.
- **MySQL**: For database management.
- **JDBC**: For connecting to MySQL.
- **JavaMail API**: For email sending and receiving.
- **Apache Commons Validator**: For email validation.

---

#### **Installation and Setup**

1. **Requirements:**
   - Java 8 or higher
   - MySQL
   - Apache Maven (optional, for dependency management)

2. **Step 1: Clone the Project**
   You can clone the project from GitHub:
   ```bash
   git clone https://github.com/engsevvalbilgin/QuizSystemProject
   ```

3. **Step 2: Configure JDBC Connection**
   In the `DatabaseConfig.java` file, add your MySQL connection details:
   ```java
   public class DatabaseConfig {
       public static final String URL = "jdbc:mysql://localhost:3306/quiz_platform";
       public static final String USER = "root";
       public static final String PASSWORD = "root_password";
   }
   ```

4. **Step 3: Configure JavaMail API**
   For email functionality, configure your email settings in `EmailUtil.java`:
   ```java
   public class EmailUtil {
       public static final String SMTP_SERVER = "smtp.gmail.com";
       public static final String SMTP_PORT = "587";
       public static final String FROM_EMAIL = "your_email@gmail.com";
       public static final String FROM_PASSWORD = "your_email_password";
   }
   ```

5. **Step 4: Run the Application**
   You can run the project from your IDE or via terminal:
   ```bash
   javac Main.java
   java Main
   ```

---

#### **User Roles**
Every user can change his/her own password, mail address etc.
1. **Student**:
   - Can participate in quizzes.
   - Can view their results.

2. **Teacher**:
   - Can create quizzes.
   - Can view and analyze results.
3. **Admin**:
   -Can update other users' properties.
---

#### **Contact**
If you find any bugs or have suggestions for improvement, please email eng.sevval.bilgin@gmail.com.

---

#### **License**
This project is licensed under the MIT License.

