package com.example.studentmanagementsystem.repository;

import com.example.studentmanagementsystem.entity.Notification;
import com.example.studentmanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    List<Notification> findByRecipientAndReadOrderByCreatedAtDesc(User recipient, boolean read);
    long countByRecipientAndRead(User recipient, boolean read);
} 