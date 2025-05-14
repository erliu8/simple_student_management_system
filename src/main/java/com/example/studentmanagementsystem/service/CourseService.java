package com.example.studentmanagementsystem.service;

import com.example.studentmanagementsystem.entity.Course;
import java.util.List;
import java.util.Optional;

public interface CourseService {
    Course saveCourse(Course course);
    Optional<Course> getCourseById(Long id);
    Optional<Course> getCourseByCourseCode(String courseCode);
    List<Course> getAllCourses();
    List<Course> getCoursesByInstructor(String instructor);
    List<Course> getCoursesBySemesterAndYear(String semester, String academicYear);
    void deleteCourse(Long id);
    boolean existsByCourseCode(String courseCode);
    long countCourses();
} 