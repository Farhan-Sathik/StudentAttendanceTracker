package com.example.AttendanceTracker.repository;

import com.example.AttendanceTracker.model.Attendance;
import com.example.AttendanceTracker.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByStudentAndDate(Student student, LocalDate date);

    List<Attendance> findByStudentAndDateBetween(Student student, LocalDate from, LocalDate to);

    List<Attendance> findByDate(LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId AND MONTH(a.date) = :month AND YEAR(a.date) = :year")
    List<Attendance> findByStudentAndMonth(@Param("studentId") Long studentId,
                                           @Param("month") int month,
                                           @Param("year") int year);

    // Needed before deleting a student — removes all their attendance records first
    @Transactional
    void deleteByStudent(Student student);
}