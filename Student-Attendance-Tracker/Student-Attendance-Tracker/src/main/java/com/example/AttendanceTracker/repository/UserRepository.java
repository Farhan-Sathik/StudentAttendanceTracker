package com.example.AttendanceTracker.repository;

import com.example.AttendanceTracker.model.Student;
import com.example.AttendanceTracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByRole(String role);

    // Needed before deleting a student — finds every teacher that has this student assigned
    List<User> findByAssignedStudentsContaining(Student student);
}