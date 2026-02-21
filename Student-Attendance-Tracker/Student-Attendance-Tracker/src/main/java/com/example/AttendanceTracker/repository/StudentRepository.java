package com.example.AttendanceTracker.repository;

import com.example.AttendanceTracker.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByClassNameContainingIgnoreCase(String className);

    @Query("SELECT s FROM Student s WHERE " +
            "LOWER(s.fullName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(s.rollNumber) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(s.className) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Student> search(@Param("q") String query);
}