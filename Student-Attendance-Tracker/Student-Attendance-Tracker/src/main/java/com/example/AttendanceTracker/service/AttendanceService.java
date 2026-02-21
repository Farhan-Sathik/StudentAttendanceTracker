package com.example.AttendanceTracker.service;

import com.example.AttendanceTracker.model.Attendance;
import com.example.AttendanceTracker.model.Attendance.AttendanceStatus;
import com.example.AttendanceTracker.model.Student;
import com.example.AttendanceTracker.model.User;
import com.example.AttendanceTracker.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepo;

    public AttendanceService(AttendanceRepository attendanceRepo) {
        this.attendanceRepo = attendanceRepo;
    }

    // Mark or update attendance for a student on a given day
    public void mark(Student student, LocalDate date, AttendanceStatus status, User teacher) {
        Optional<Attendance> existing = attendanceRepo.findByStudentAndDate(student, date);
        Attendance record = existing.orElse(new Attendance());
        record.setStudent(student);
        record.setDate(date);
        record.setStatus(status);
        record.setMarkedBy(teacher);
        attendanceRepo.save(record);
    }

    public Optional<Attendance> getRecord(Student student, LocalDate date) {
        return attendanceRepo.findByStudentAndDate(student, date);
    }

    // Returns attendance records for a student in a specific month/year
    public List<Attendance> getMonthlyReport(Long studentId, int month, int year) {
        return attendanceRepo.findByStudentAndMonth(studentId, month, year);
    }

    // Get all records for a specific date
    public List<Attendance> getByDate(LocalDate date) {
        return attendanceRepo.findByDate(date);
    }

    // Calculate present percentage
    public double getPresentPercentage(Long studentId, int month, int year) {
        List<Attendance> records = getMonthlyReport(studentId, month, year);
        if (records.isEmpty()) return 0.0;
        long presentCount = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
        return Math.round((presentCount * 100.0 / records.size()) * 10.0) / 10.0;
    }
}