package com.example.AttendanceTracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;

    // ADMIN or TEACHER
    private String role;

    // Teacher's assigned students (only used when role = TEACHER)
    @ManyToMany
    @JoinTable(
            name = "teacher_student_map",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> assignedStudents;
}