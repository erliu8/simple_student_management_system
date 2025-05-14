package com.example.studentmanagementsystem.controller;

import com.example.studentmanagementsystem.service.CourseService;
import com.example.studentmanagementsystem.service.StudentService;
import com.example.studentmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final UserService userService;

    @Autowired
    public HomeController(StudentService studentService, CourseService courseService, UserService userService) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Add dashboard data
        model.addAttribute("totalStudents", studentService.countStudents());
        model.addAttribute("totalCourses", courseService.countCourses());
        model.addAttribute("totalUsers", userService.countUsers());
        
        // You can add more data like recent activities if needed
        
        return "dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
} 