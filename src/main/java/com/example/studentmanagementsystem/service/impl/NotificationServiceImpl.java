package com.example.studentmanagementsystem.service.impl;

import com.example.studentmanagementsystem.entity.Notification;
import com.example.studentmanagementsystem.entity.User;
import com.example.studentmanagementsystem.repository.NotificationRepository;
import com.example.studentmanagementsystem.service.NotificationService;
import com.example.studentmanagementsystem.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserService userService;
    
    @Override
    public Notification createNotification(String title, String message, String senderUsername, User recipient, String entityType, Long entityId) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setSenderUsername(senderUsername);
        notification.setRecipient(recipient);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notification.setRead(false);
        
        return notificationRepository.save(notification);
    }
    
    @Override
    public List<Notification> createNotificationsForUsers(String title, String message, String senderUsername, List<User> recipients, String entityType, Long entityId) {
        List<Notification> notifications = new ArrayList<>();
        
        for (User recipient : recipients) {
            Notification notification = new Notification();
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setSenderUsername(senderUsername);
            notification.setRecipient(recipient);
            notification.setEntityType(entityType);
            notification.setEntityId(entityId);
            notification.setRead(false);
            
            notifications.add(notification);
        }
        
        return notificationRepository.saveAll(notifications);
    }
    
    @Override
    public List<Notification> createNotificationsForAllStudents(String title, String message, String senderUsername, String entityType, Long entityId) {
        // 获取所有有STUDENT角色的用户
        List<User> students = userService.getAllUsers().stream()
                .filter(user -> user.getRoles().contains("STUDENT") || user.getRoles().contains("ROLE_STUDENT"))
                .collect(Collectors.toList());
        
        return createNotificationsForUsers(title, message, senderUsername, students, entityType, entityId);
    }
    
    @Override
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }
    
    @Override
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByRecipientAndReadOrderByCreatedAtDesc(user, false);
    }
    
    @Override
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);
            return notificationRepository.save(notification);
        }
        return null;
    }
    
    @Override
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientAndReadOrderByCreatedAtDesc(user, false);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
    
    @Override
    public long getUnreadNotificationsCount(User user) {
        return notificationRepository.countByRecipientAndRead(user, false);
    }
    
    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
} 