package com.example.AttendanceTracker.controller;

import com.example.AttendanceTracker.model.Attendance;
import com.example.AttendanceTracker.model.Attendance.AttendanceStatus;
import com.example.AttendanceTracker.model.Student;
import com.example.AttendanceTracker.model.User;
import com.example.AttendanceTracker.service.*;
import com.example.AttendanceTracker.service.StudentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final UserService userService;
    private final AttendanceService attendanceService;
    private final StudentService studentService;

    public TeacherController(UserService userService, AttendanceService attendanceService,
                             StudentService studentService) {
        this.userService = userService;
        this.attendanceService = attendanceService;
        this.studentService = studentService;
    }

    private User getCurrentTeacher(Authentication auth) {
        return userService.getByUsername(auth.getName());
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User teacher = getCurrentTeacher(auth);
        List<Student> students = teacher.getAssignedStudents();
        model.addAttribute("studentCount", students == null ? 0 : students.size());
        model.addAttribute("teacherName", teacher.getFullName());
        return "teacher/dashboard";
    }

    @GetMapping("/attendance")
    public String attendancePage(Authentication auth,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 @RequestParam(required = false) String success,
                                 Model model) {
        if (date == null) date = LocalDate.now();
        User teacher = getCurrentTeacher(auth);
        List<Student> students = teacher.getAssignedStudents();

        // Build a map: studentId -> existing status (if already marked)
        Map<Long, String> existing = new HashMap<>();
        if (students != null) {
            for (Student s : students) {
                attendanceService.getRecord(s, date)
                        .ifPresent(a -> existing.put(s.getId(), a.getStatus().name()));
            }
        }

        model.addAttribute("students", students);
        model.addAttribute("date", date);
        model.addAttribute("existingStatus", existing);
        // FIX 1: pass success flag to template
        model.addAttribute("successMessage", "true".equals(success)
                ? "Attendance saved successfully for " + date + "!" : null);
        return "teacher/attendance";
    }

    @PostMapping("/attendance")
    public String submitAttendance(Authentication auth,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam Map<String, String> params) {
        User teacher = getCurrentTeacher(auth);
        // params will have keys like "status_1", "status_2" etc.
        params.forEach((key, value) -> {
            if (key.startsWith("status_")) {
                Long studentId = Long.parseLong(key.replace("status_", ""));
                teacher.getAssignedStudents().stream()
                        .filter(s -> s.getId().equals(studentId))
                        .findFirst()
                        .ifPresent(student -> {
                            AttendanceStatus status = value.equals("PRESENT")
                                    ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT;
                            attendanceService.mark(student, date, status, teacher);
                        });
            }
        });
        // FIX 1: redirect with success=true so the GET handler shows the message
        return "redirect:/teacher/attendance?date=" + date + "&success=true";
    }

    @GetMapping("/reports")
    public String reports(Authentication auth,
                          @RequestParam(required = false) Long studentId,
                          @RequestParam(required = false) Integer month,
                          @RequestParam(required = false) Integer year,
                          Model model) {
        User teacher = getCurrentTeacher(auth);
        List<Student> students = teacher.getAssignedStudents();
        if (month == null) month = LocalDate.now().getMonthValue();
        if (year == null) year = LocalDate.now().getYear();

        model.addAttribute("students", students);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("monthName", Month.of(month).name());
        model.addAttribute("selectedStudentId", studentId);

        if (studentId != null) {
            // FIX 2: verify the requested student actually belongs to this teacher
            boolean isAssigned = students != null && students.stream()
                    .anyMatch(s -> s.getId().equals(studentId));

            if (isAssigned) {
                List<Attendance> records = attendanceService.getMonthlyReport(studentId, month, year);
                double percentage = attendanceService.getPresentPercentage(studentId, month, year);
                double absentPct = Math.round((100.0 - percentage) * 10.0) / 10.0;
                model.addAttribute("records", records);
                model.addAttribute("percentage", percentage);
                model.addAttribute("absentPercentage", absentPct);

                // FIX 2: pass student name directly instead of resolving it in Thymeleaf
                studentService.getById(studentId).ifPresent(s ->
                        model.addAttribute("selectedStudentName", s.getFullName()));
            }
        }

        return "teacher/reports";
    }
}