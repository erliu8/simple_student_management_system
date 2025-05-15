package com.example.studentmanagementsystem.repository;

import com.example.studentmanagementsystem.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findByOperatorUsernameOrderByOperationTimeDesc(String username);
    List<SystemLog> findByOperationTypeOrderByOperationTimeDesc(String operationType);
    List<SystemLog> findTop100ByOrderByOperationTimeDesc();
} 