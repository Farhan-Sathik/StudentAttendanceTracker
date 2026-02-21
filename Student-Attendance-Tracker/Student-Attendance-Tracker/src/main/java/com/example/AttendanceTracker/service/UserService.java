package com.example.AttendanceTracker.service;

import com.example.AttendanceTracker.model.Student;
import com.example.AttendanceTracker.model.User;
import com.example.AttendanceTracker.repository.StudentRepository;
import com.example.AttendanceTracker.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    // Spring Security calls this during login
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }

    public List<User> getAllTeachers() {
        return userRepo.findByRole("TEACHER");
    }

    public Optional<User> getById(Long id) {
        return userRepo.findById(id);
    }

    public User getByUsername(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }

    public void saveTeacher(User teacher) {
        teacher.setRole("TEACHER");
        teacher.setPassword(encoder.encode(teacher.getPassword()));
        userRepo.save(teacher);
    }

    public void updateTeacher(Long id, User updated) {
        User existing = userRepo.findById(id).orElseThrow();
        existing.setFullName(updated.getFullName());
        existing.setUsername(updated.getUsername());
        // Only re-encode if a new password was provided
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(encoder.encode(updated.getPassword()));
        }
        userRepo.save(existing);
    }

    /**
     * Safe delete — clears the teacher's student assignments first so the
     * teacher_student_map rows are removed, then deletes the user row.
     * Without this the DB throws a FK constraint error on the delete.
     */
    @Transactional
    public void deleteTeacher(Long id) {
        User teacher = userRepo.findById(id).orElse(null);
        if (teacher == null) return;

        // Clear assignments so the join table rows are removed
        teacher.setAssignedStudents(new ArrayList<>());
        userRepo.save(teacher);

        // Now safe to delete
        userRepo.deleteById(id);
    }

    public void assignStudents(Long teacherId, List<Long> studentIds, StudentRepository studentRepo) {
        User teacher = userRepo.findById(teacherId).orElseThrow();
        List<Student> students = studentRepo.findAllById(studentIds);
        teacher.setAssignedStudents(students);
        userRepo.save(teacher);
    }
}