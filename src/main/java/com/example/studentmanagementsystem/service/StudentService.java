package com.example.studentmanagementsystem.service;

import com.example.studentmanagementsystem.entity.Student;
import java.util.List;
import java.util.Optional;

public interface StudentService {
    Student saveStudent(Student student);
    Optional<Student> getStudentById(Long id);
    Optional<Student> getStudentByStudentNumber(String studentNumber);
    List<Student> getAllStudents();
    List<Student> getStudentsByClassName(String className);
    void deleteStudent(Long id);
    boolean existsByStudentNumber(String studentNumber);
    long countStudents();
} 