package com.example.AttendanceTracker.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String rollNumber;

    private String className;   // e.g. "10-A", "9-B"

    private String email;

    private String phone;
}