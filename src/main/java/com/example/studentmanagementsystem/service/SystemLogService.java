package com.example.studentmanagementsystem.service;

import com.example.studentmanagementsystem.entity.SystemLog;
import com.example.studentmanagementsystem.entity.User;

import java.util.List;

public interface SystemLogService {
    // 记录系统操作日志
    SystemLog logOperation(String operationType, String operationDesc, String operatorUsername, String operationResult, String ipAddress);
    
    // 获取最近的系统日志
    List<SystemLog> getRecentLogs(int limit);
    
    // 获取用户的操作日志
    List<SystemLog> getUserLogs(String username, int limit);
    
    // 获取特定类型的操作日志
    List<SystemLog> getLogsByType(String operationType, int limit);
} 