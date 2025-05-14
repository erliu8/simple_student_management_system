package com.example.studentmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_log")
public class SystemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_type", nullable = false)
    private String operationType;

    @Column(name = "operation_desc", nullable = false)
    private String operationDesc;

    @Column(name = "operator_username", nullable = false)
    private String operatorUsername;

    @Column(name = "operation_time", nullable = false)
    private java.time.LocalDateTime operationTime;

    @Column(name = "operation_result")
    private String operationResult;

    @Column(name = "ip_address")
    private String ipAddress;
} 