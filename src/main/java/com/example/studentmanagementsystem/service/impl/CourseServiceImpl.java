package com.example.studentmanagementsystem.service.impl;

import com.example.studentmanagementsystem.entity.Course;
import com.example.studentmanagementsystem.repository.CourseRepository;
import com.example.studentmanagementsystem.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Autowired
    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public Optional<Course> getCourseByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode);
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public List<Course> getCoursesByInstructor(String instructor) {
        return courseRepository.findByInstructor(instructor);
    }

    @Override
    public List<Course> getCoursesBySemesterAndYear(String semester, String academicYear) {
        return courseRepository.findBySemesterAndAcademicYear(semester, academicYear);
    }

    @Override
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    public boolean existsByCourseCode(String courseCode) {
        return courseRepository.existsByCourseCode(courseCode);
    }

    @Override
    public long countCourses() {
        return courseRepository.count();
    }
} 