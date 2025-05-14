package com.example.studentmanagementsystem.service;

import com.example.studentmanagementsystem.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);
    Optional<User> getUserById(Long id);
    Optional<User> findByUsername(String username);
    List<User> getAllUsers();
    void deleteUser(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByIdCard(String idCard);
    long countUsers();
} 