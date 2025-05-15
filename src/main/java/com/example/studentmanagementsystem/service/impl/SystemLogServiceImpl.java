package com.example.studentmanagementsystem.service.impl;

import com.example.studentmanagementsystem.entity.SystemLog;
import com.example.studentmanagementsystem.repository.SystemLogRepository;
import com.example.studentmanagementsystem.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemLogServiceImpl implements SystemLogService {

    @Autowired
    private SystemLogRepository systemLogRepository;

    @Override
    public SystemLog logOperation(String operationType, String operationDesc, String operatorUsername, 
                                String operationResult, String ipAddress) {
        SystemLog log = new SystemLog();
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        log.setOperatorUsername(operatorUsername);
        log.setOperationTime(LocalDateTime.now());
        log.setOperationResult(operationResult);
        log.setIpAddress(ipAddress);
        
        return systemLogRepository.save(log);
    }

    @Override
    public List<SystemLog> getRecentLogs(int limit) {
        List<SystemLog> logs = systemLogRepository.findTop100ByOrderByOperationTimeDesc();
        return logs.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<SystemLog> getUserLogs(String username, int limit) {
        List<SystemLog> logs = systemLogRepository.findByOperatorUsernameOrderByOperationTimeDesc(username);
        return logs.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<SystemLog> getLogsByType(String operationType, int limit) {
        List<SystemLog> logs = systemLogRepository.findByOperationTypeOrderByOperationTimeDesc(operationType);
        return logs.stream().limit(limit).collect(Collectors.toList());
    }
} 