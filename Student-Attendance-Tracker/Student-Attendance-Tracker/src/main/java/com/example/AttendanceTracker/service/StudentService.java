package com.example.AttendanceTracker.service;

import com.example.AttendanceTracker.model.Student;
import com.example.AttendanceTracker.model.User;
import com.example.AttendanceTracker.repository.AttendanceRepository;
import com.example.AttendanceTracker.repository.StudentRepository;
import com.example.AttendanceTracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepo;
    private final AttendanceRepository attendanceRepo;
    private final UserRepository userRepo;

    public StudentService(StudentRepository studentRepo,
                          AttendanceRepository attendanceRepo,
                          UserRepository userRepo) {
        this.studentRepo = studentRepo;
        this.attendanceRepo = attendanceRepo;
        this.userRepo = userRepo;
    }

    public List<Student> getAll() {
        return studentRepo.findAll();
    }

    public Optional<Student> getById(Long id) {
        return studentRepo.findById(id);
    }

    public void save(Student student) {
        studentRepo.save(student);
    }

    /**
     * Safe delete — clears the student from:
     * 1. All attendance records  (attendance table FK)
     * 2. All teacher assignments (teacher_student_map FK)
     * Then deletes the student row itself.
     *
     * Without this order the DB throws a constraint violation and the
     * delete button shows a 500 error page.
     */
    @Transactional
    public void delete(Long id) {
        Student student = studentRepo.findById(id).orElse(null);
        if (student == null) return;

        // Step 1: Remove all attendance records for this student
        attendanceRepo.deleteByStudent(student);

        // Step 2: Remove student from every teacher's assigned list
        List<User> teachers = userRepo.findByAssignedStudentsContaining(student);
        for (User teacher : teachers) {
            teacher.getAssignedStudents().remove(student);
            userRepo.save(teacher);
        }

        // Step 3: Now safe to delete
        studentRepo.deleteById(id);
    }

    public List<Student> search(String query) {
        if (query == null || query.isBlank()) return studentRepo.findAll();
        return studentRepo.search(query);
    }
}