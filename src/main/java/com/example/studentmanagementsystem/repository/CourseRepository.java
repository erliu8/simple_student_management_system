package com.example.studentmanagementsystem.repository;

import com.example.studentmanagementsystem.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    List<Course> findByInstructor(String instructor);
    List<Course> findBySemesterAndAcademicYear(String semester, String academicYear);
    List<Course> findByStatus(String status);
    boolean existsByCourseCode(String courseCode);
} 