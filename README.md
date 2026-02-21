# Student Attendance Tracker

A web-based application for institutions to manage students, assign them to teachers, and track daily attendance — with role-based access for **Admin** and **Teacher**.

---

## Features

### Admin
- Add / Edit / Delete Teachers
- Add / Edit / Delete Students
- Assign students to teachers
- View dashboard with key stats (total teachers, students, today's attendance count)
- View attendance reports for any student with monthly summaries

### Teacher
- View only their assigned students
- Mark daily attendance (Present / Absent) with date selection
- Mark all present or all absent with a single click
- View attendance reports for their assigned students
- Duplicate attendance for the same day is prevented (updates existing record)

---

## Tech Stack

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Frontend  | HTML, Bootstrap 5.3, Bootstrap Icons, Thymeleaf |
| Backend   | Spring Boot 3.2 (Spring MVC, Spring Security) |
| Database  | MySQL                               |
| ORM       | Spring Data JPA / Hibernate         |
| Build     | Maven                               |
| Language  | Java 17                             |

---

## Prerequisites

Make sure the following are installed before running the project:

- **Java 17** or higher — [Download](https://adoptium.net/)
- **Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** — [Download](https://dev.mysql.com/downloads/)
- An IDE like **IntelliJ IDEA** or **Eclipse** (optional but recommended)

---

## Database Setup

1. Open your MySQL client (MySQL Workbench, DBeaver, or terminal).
2. Create the database:

```sql
CREATE DATABASE attendance_db;
```

> The application uses `spring.jpa.hibernate.ddl-auto=update`, so all tables are created automatically on first run. You do **not** need to run any SQL scripts manually.

---

## Configuration

Open the file:

```
src/main/resources/application.properties
```

Update the database credentials to match your local MySQL setup:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/attendance_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Leave `spring.datasource.password=` blank if your MySQL root user has no password.

---

## Running the Application

### Option 1 — Using Maven Wrapper (recommended)

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

### Option 2 — Using Maven directly

```bash
mvn spring-boot:run
```

### Option 3 — Build a JAR and run it

```bash
mvn clean package
java -jar target/student-attendance-tracker-1.0.0.jar
```

Once started, open your browser and go to:

```
http://localhost:8080
```

---

## Default Login Credentials

A default admin account is created automatically on the first run.

| Role  | Username | Password  |
|-------|----------|-----------|
| Admin | `admin`  | `admin123` |

> **Important:** Change the admin password after your first login.

Teacher accounts are created by the Admin through the **Manage Teachers** page. There are no default teacher credentials.

---

## Project Structure

```
src/
└── main/
    ├── java/com/example/AttendanceTracker/
    │   ├── AttendanceTrackerApplication.java   # Entry point
    │   ├── config/
    │   │   ├── SecurityConfig.java             # Spring Security setup & role-based access
    │   │   └── DataInitializer.java            # Creates default admin on startup
    │   ├── controller/
    │   │   ├── AuthController.java             # Login / logout
    │   │   ├── AdminController.java            # Admin panel routes
    │   │   └── TeacherController.java          # Teacher panel routes
    │   ├── model/
    │   │   ├── User.java                       # Admin & Teacher accounts
    │   │   ├── Student.java                    # Student records
    │   │   └── Attendance.java                 # Daily attendance records
    │   ├── repository/
    │   │   ├── UserRepository.java
    │   │   ├── StudentRepository.java
    │   │   └── AttendanceRepository.java
    │   └── service/
    │       ├── UserService.java
    │       ├── StudentService.java
    │       └── AttendanceService.java
    └── resources/
        ├── application.properties
        └── templates/
            ├── login.html
            ├── admin/
            │   ├── dashboard.html
            │   ├── teachers.html
            │   ├── students.html
            │   └── reports.html
            └── teacher/
                ├── dashboard.html
                ├── attendance.html
                └── reports.html
```

---

## Database Tables

These tables are auto-created by Hibernate on startup:

| Table                | Description                                  |
|----------------------|----------------------------------------------|
| `users`              | Stores Admin and Teacher login accounts      |
| `students`           | Stores student details                       |
| `attendance`         | Daily attendance records (Present / Absent)  |
| `teacher_student_map`| Many-to-many mapping of teachers to students |

### Attendance Table Schema

| Column       | Type         | Description                    |
|--------------|--------------|--------------------------------|
| `id`         | BIGINT (PK)  | Auto-generated primary key     |
| `student_id` | BIGINT (FK)  | References `students.id`       |
| `date`       | DATE         | Attendance date                |
| `status`     | ENUM         | `PRESENT` or `ABSENT`          |
| `marked_by`  | BIGINT (FK)  | References `users.id` (teacher)|

---

## URL Reference

| URL                    | Access       | Description                        |
|------------------------|--------------|------------------------------------|
| `/login`               | All          | Login page                         |
| `/logout`              | All          | Logs out the current user          |
| `/admin/dashboard`     | Admin        | Admin dashboard with stats         |
| `/admin/teachers`      | Admin        | Add / Edit / Delete teachers       |
| `/admin/students`      | Admin        | Add / Edit / Delete students       |
| `/admin/reports`       | Admin        | View attendance reports            |
| `/teacher/dashboard`   | Teacher      | Teacher dashboard                  |
| `/teacher/attendance`  | Teacher      | Mark daily attendance              |
| `/teacher/reports`     | Teacher      | View reports for assigned students |

---

## Common Issues & Fixes

**Application fails to start — database connection error**
- Confirm MySQL is running.
- Double-check `spring.datasource.username` and `spring.datasource.password` in `application.properties`.
- Ensure the `attendance_db` database exists, or add `createDatabaseIfNotExist=true` to the datasource URL (already included by default).

**Port 8080 already in use**
- Change the port in `application.properties`:
  ```properties
  server.port=9090
  ```

**Tables not created automatically**
- Ensure `spring.jpa.hibernate.ddl-auto=update` is set in `application.properties`.

---

## Security

- Passwords are encrypted using **BCrypt** before being stored in the database.
- Role-based access control is enforced by Spring Security — teachers cannot access admin routes and vice versa.
- CSRF protection is enabled for all POST forms via Thymeleaf's built-in Spring Security integration.

---

## License

This project is intended for educational and institutional use.
