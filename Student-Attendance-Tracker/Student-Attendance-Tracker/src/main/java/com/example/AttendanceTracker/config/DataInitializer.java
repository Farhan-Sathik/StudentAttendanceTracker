package com.example.AttendanceTracker.config;

import com.example.AttendanceTracker.model.User;
import com.example.AttendanceTracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs once at startup. Creates a default admin account if none exists.
 * Default credentials:  username = admin  /  password = admin123
 * Change the password after first login (or update the value below before deploying).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setFullName("System Administrator");
            admin.setRole("ADMIN");
            admin.setPassword(passwordEncoder.encode("admin123"));
            userRepo.save(admin);
            System.out.println("==============================================");
            System.out.println("  Default admin created: admin / admin123");
            System.out.println("  Please change the password after first login.");
            System.out.println("==============================================");
        }
    }
}
