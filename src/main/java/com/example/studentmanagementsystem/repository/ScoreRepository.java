package com.example.studentmanagementsystem.repository;

import com.example.studentmanagementsystem.entity.Score;
import com.example.studentmanagementsystem.entity.Student;
import com.example.studentmanagementsystem.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByStudent(Student student);
    List<Score> findByCourse(Course course);
    Optional<Score> findByStudentAndCourse(Student student, Course course);
    List<Score> findByStudentAndGradeLetter(Student student, String gradeLetter);
    List<Score> findByCourseAndGradeLetter(Course course, String gradeLetter);
} 