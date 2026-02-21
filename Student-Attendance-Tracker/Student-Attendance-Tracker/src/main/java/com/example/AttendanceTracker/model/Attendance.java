package com.example.AttendanceTracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "date"}))
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate date;

    // "PRESENT" or "ABSENT"
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @ManyToOne
    @JoinColumn(name = "marked_by")
    private User markedBy;

    public enum AttendanceStatus {
        PRESENT, ABSENT
    }
}