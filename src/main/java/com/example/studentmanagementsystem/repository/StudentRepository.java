package com.example.studentmanagementsystem.repository;

import com.example.studentmanagementsystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByEmail(String email);
    List<Student> findByMajor(String major);
    List<Student> findByStatus(String status);
    boolean existsByStudentId(String studentId);
    boolean existsByEmail(String email);
} 