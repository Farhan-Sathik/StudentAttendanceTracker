package com.example.AttendanceTracker.controller;

import com.example.AttendanceTracker.model.Attendance;
import com.example.AttendanceTracker.model.Student;
import com.example.AttendanceTracker.model.User;
import com.example.AttendanceTracker.repository.StudentRepository;
import com.example.AttendanceTracker.service.AttendanceService;
import com.example.AttendanceTracker.service.StudentService;
import com.example.AttendanceTracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final StudentRepository studentRepo;

    public AdminController(UserService userService, StudentService studentService,
                           AttendanceService attendanceService, StudentRepository studentRepo) {
        this.userService = userService;
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.studentRepo = studentRepo;
    }

    // ── Dashboard ─────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("teacherCount", userService.getAllTeachers().size());
        model.addAttribute("studentCount", studentService.getAll().size());
        long todayCount = attendanceService.getByDate(LocalDate.now()).size();
        model.addAttribute("todayCount", todayCount);
        return "admin/dashboard";
    }

    // ── Teachers ──────────────────────────────────────────────
    @GetMapping("/teachers")
    public String teachers(Model model) {
        model.addAttribute("teachers", userService.getAllTeachers());
        model.addAttribute("allStudents", studentService.getAll());
        return "admin/teachers";
    }

    @PostMapping("/teachers/add")
    public String addTeacher(@RequestParam String fullName,
                             @RequestParam String username,
                             @RequestParam String password) {
        User teacher = new User();
        teacher.setFullName(fullName);
        teacher.setUsername(username);
        teacher.setPassword(password);
        userService.saveTeacher(teacher);
        return "redirect:/admin/teachers";
    }

    @PostMapping("/teachers/edit/{id}")
    public String editTeacher(@PathVariable Long id,
                              @RequestParam String fullName,
                              @RequestParam String username,
                              @RequestParam(required = false) String password) {
        User updated = new User();
        updated.setFullName(fullName);
        updated.setUsername(username);
        updated.setPassword(password);
        userService.updateTeacher(id, updated);
        return "redirect:/admin/teachers";
    }

    @GetMapping("/teachers/delete/{id}")
    public String deleteTeacher(@PathVariable Long id) {
        userService.deleteTeacher(id);
        return "redirect:/admin/teachers";
    }

    @PostMapping("/teachers/assign/{id}")
    public String assignStudents(@PathVariable Long id,
                                 @RequestParam(required = false) List<Long> studentIds) {
        userService.assignStudents(id, studentIds == null ? List.of() : studentIds, studentRepo);
        return "redirect:/admin/teachers";
    }

    /**
     * REST endpoint used by the Assign Students modal via fetch().
     * Returns the list of student IDs currently assigned to the given teacher.
     */
    @GetMapping("/teachers/{id}/assigned")
    @ResponseBody
    public List<Long> getAssignedStudentIds(@PathVariable Long id) {
        return userService.getById(id)
                .map(teacher -> {
                    if (teacher.getAssignedStudents() == null) return Collections.<Long>emptyList();
                    return teacher.getAssignedStudents().stream()
                            .map(Student::getId)
                            .collect(Collectors.toList());
                })
                .orElse(Collections.emptyList());
    }

    // ── Students ──────────────────────────────────────────────
    @GetMapping("/students")
    public String students(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("students", studentService.search(q));
        model.addAttribute("query", q != null ? q : "");
        return "admin/students";
    }

    @PostMapping("/students/add")
    public String addStudent(@RequestParam String fullName,
                             @RequestParam(required = false) String rollNumber,
                             @RequestParam(required = false) String className,
                             @RequestParam(required = false) String email,
                             @RequestParam(required = false) String phone) {
        Student student = new Student();
        student.setFullName(fullName);
        student.setRollNumber(rollNumber);
        student.setClassName(className);
        student.setEmail(email);
        student.setPhone(phone);
        studentService.save(student);
        return "redirect:/admin/students";
    }

    @PostMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id,
                              @RequestParam String fullName,
                              @RequestParam(required = false) String rollNumber,
                              @RequestParam(required = false) String className,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String phone) {
        Student student = studentService.getById(id).orElseThrow();
        student.setFullName(fullName);
        student.setRollNumber(rollNumber);
        student.setClassName(className);
        student.setEmail(email);
        student.setPhone(phone);
        studentService.save(student);
        return "redirect:/admin/students";
    }

    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentService.delete(id);
        return "redirect:/admin/students";
    }

    // ── Reports (Admin) ───────────────────────────────────────
    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) Long studentId,
                          @RequestParam(required = false) Long teacherId,
                          @RequestParam(required = false) Integer month,
                          @RequestParam(required = false) Integer year,
                          Model model) {

        if (month == null) month = LocalDate.now().getMonthValue();
        if (year == null) year = LocalDate.now().getYear();

        model.addAttribute("allStudents", studentService.getAll());
        model.addAttribute("allTeachers", userService.getAllTeachers());
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedStudentId", studentId);
        model.addAttribute("selectedTeacherId", teacherId);
        model.addAttribute("monthName", Month.of(month).name());

        // If teacher filter is active, narrow the student list shown
        if (teacherId != null) {
            userService.getById(teacherId).ifPresent(t -> {
                List<Student> teacherStudents = t.getAssignedStudents();
                model.addAttribute("filteredStudents",
                        teacherStudents != null ? teacherStudents : List.of());
            });
        }

        if (studentId != null) {
            List<Attendance> records = attendanceService.getMonthlyReport(studentId, month, year);
            double percentage = attendanceService.getPresentPercentage(studentId, month, year);
            double absentPct = Math.round((100.0 - percentage) * 10.0) / 10.0;
            model.addAttribute("records", records);
            model.addAttribute("percentage", percentage);
            model.addAttribute("absentPercentage", absentPct);

            studentService.getById(studentId).ifPresent(s ->
                    model.addAttribute("selectedStudentName", s.getFullName()));
        }

        return "admin/reports";
    }
}
