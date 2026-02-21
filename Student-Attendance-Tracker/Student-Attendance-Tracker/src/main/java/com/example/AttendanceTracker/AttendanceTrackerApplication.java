package com.example.AttendanceTracker;

import com.example.AttendanceTracker.model.User;
import com.example.AttendanceTracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AttendanceTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceTrackerApplication.class, args);
    }

    // This creates a default admin on first run if none exists
    @Bean
    CommandLineRunner initAdmin(UserRepository userRepo, PasswordEncoder encoder) {
        return args -> {
            if (userRepo.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setFullName("System Admin");
                admin.setRole("ADMIN");
                userRepo.save(admin);
                System.out.println("Default admin created → username: admin | password: admin123");
            }
        };
    }
}