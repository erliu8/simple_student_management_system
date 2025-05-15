package com.example.studentmanagementsystem.service;

import com.example.studentmanagementsystem.entity.Notification;
import com.example.studentmanagementsystem.entity.User;

import java.util.List;

public interface NotificationService {
    
    // 创建针对单个用户的通知
    Notification createNotification(String title, String message, String senderUsername, User recipient, String entityType, Long entityId);
    
    // 创建针对多个用户的通知
    List<Notification> createNotificationsForUsers(String title, String message, String senderUsername, List<User> recipients, String entityType, Long entityId);
    
    // 创建针对所有学生的通知
    List<Notification> createNotificationsForAllStudents(String title, String message, String senderUsername, String entityType, Long entityId);
    
    // 获取用户的所有通知
    List<Notification> getUserNotifications(User user);
    
    // 获取用户的未读通知
    List<Notification> getUnreadNotifications(User user);
    
    // 标记通知为已读
    Notification markAsRead(Long notificationId);
    
    // 标记用户所有通知为已读
    void markAllAsRead(User user);
    
    // 获取用户未读通知数量
    long getUnreadNotificationsCount(User user);
    
    // 删除通知
    void deleteNotification(Long id);
} 