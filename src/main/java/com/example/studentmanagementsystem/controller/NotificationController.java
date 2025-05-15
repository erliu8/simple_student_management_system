package com.example.studentmanagementsystem.controller;

import com.example.studentmanagementsystem.entity.Notification;
import com.example.studentmanagementsystem.entity.User;
import com.example.studentmanagementsystem.service.NotificationService;
import com.example.studentmanagementsystem.service.SystemLogService;
import com.example.studentmanagementsystem.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    private final SystemLogService systemLogService;
    
    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService, 
                                 SystemLogService systemLogService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.systemLogService = systemLogService;
    }
    
    // 获取当前登录用户
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymousUser";
    }
    
    // 获取当前登录用户对象
    private Optional<User> getCurrentUser() {
        String username = getCurrentUsername();
        if ("anonymousUser".equals(username)) {
            return Optional.empty();
        }
        return userService.findByUsername(username);
    }
    
    @GetMapping
    public String viewNotifications(Model model, HttpServletRequest request) {
        Optional<User> currentUser = getCurrentUser();
        
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }
        
        User user = currentUser.get();
        
        // 获取用户的所有通知
        List<Notification> notifications = notificationService.getUserNotifications(user);
        
        // 记录系统日志
        systemLogService.logOperation(
            "VIEW_NOTIFICATIONS", 
            "用户查看通知列表", 
            getCurrentUsername(), 
            "SUCCESS", 
            request.getRemoteAddr()
        );
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notificationService.getUnreadNotificationsCount(user));
        
        return "notification/list";
    }
    
    @GetMapping("/unread")
    public String viewUnreadNotifications(Model model, HttpServletRequest request) {
        Optional<User> currentUser = getCurrentUser();
        
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }
        
        User user = currentUser.get();
        
        // 获取用户的未读通知
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(user);
        
        // 记录系统日志
        systemLogService.logOperation(
            "VIEW_UNREAD_NOTIFICATIONS", 
            "用户查看未读通知", 
            getCurrentUsername(), 
            "SUCCESS", 
            request.getRemoteAddr()
        );
        
        model.addAttribute("notifications", unreadNotifications);
        model.addAttribute("unreadCount", unreadNotifications.size());
        model.addAttribute("unreadOnly", true);
        
        return "notification/list";
    }
    
    @PostMapping("/{id}/mark-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable("id") Long id, HttpServletRequest request) {
        Optional<User> currentUser = getCurrentUser();
        Map<String, Object> response = new HashMap<>();
        
        if (currentUser.isEmpty()) {
            response.put("success", false);
            response.put("message", "用户未登录");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            Notification notification = notificationService.markAsRead(id);
            
            if (notification != null) {
                // 记录系统日志
                systemLogService.logOperation(
                    "MARK_NOTIFICATION_READ", 
                    "用户标记通知为已读：ID " + id, 
                    getCurrentUsername(), 
                    "SUCCESS", 
                    request.getRemoteAddr()
                );
                
                response.put("success", true);
                response.put("message", "通知已标记为已读");
                response.put("unreadCount", notificationService.getUnreadNotificationsCount(currentUser.get()));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "未找到指定通知");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            // 记录系统日志
            systemLogService.logOperation(
                "MARK_NOTIFICATION_READ", 
                "用户标记通知为已读失败：" + e.getMessage(), 
                getCurrentUsername(), 
                "ERROR", 
                request.getRemoteAddr()
            );
            
            response.put("success", false);
            response.put("message", "操作失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpServletRequest request) {
        Optional<User> currentUser = getCurrentUser();
        Map<String, Object> response = new HashMap<>();
        
        if (currentUser.isEmpty()) {
            response.put("success", false);
            response.put("message", "用户未登录");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            notificationService.markAllAsRead(currentUser.get());
            
            // 记录系统日志
            systemLogService.logOperation(
                "MARK_ALL_NOTIFICATIONS_READ", 
                "用户标记所有通知为已读", 
                getCurrentUsername(), 
                "SUCCESS", 
                request.getRemoteAddr()
            );
            
            response.put("success", true);
            response.put("message", "所有通知已标记为已读");
            response.put("unreadCount", 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 记录系统日志
            systemLogService.logOperation(
                "MARK_ALL_NOTIFICATIONS_READ", 
                "用户标记所有通知为已读失败：" + e.getMessage(), 
                getCurrentUsername(), 
                "ERROR", 
                request.getRemoteAddr()
            );
            
            response.put("success", false);
            response.put("message", "操作失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable("id") Long id, HttpServletRequest request) {
        Optional<User> currentUser = getCurrentUser();
        Map<String, Object> response = new HashMap<>();
        
        if (currentUser.isEmpty()) {
            response.put("success", false);
            response.put("message", "用户未登录");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            notificationService.deleteNotification(id);
            
            // 记录系统日志
            systemLogService.logOperation(
                "DELETE_NOTIFICATION", 
                "用户删除通知：ID " + id, 
                getCurrentUsername(), 
                "SUCCESS", 
                request.getRemoteAddr()
            );
            
            response.put("success", true);
            response.put("message", "通知已删除");
            response.put("unreadCount", notificationService.getUnreadNotificationsCount(currentUser.get()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 记录系统日志
            systemLogService.logOperation(
                "DELETE_NOTIFICATION", 
                "用户删除通知失败：" + e.getMessage(), 
                getCurrentUsername(), 
                "ERROR", 
                request.getRemoteAddr()
            );
            
            response.put("success", false);
            response.put("message", "操作失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        Optional<User> currentUser = getCurrentUser();
        Map<String, Object> response = new HashMap<>();
        
        if (currentUser.isEmpty()) {
            response.put("unreadCount", 0);
            return ResponseEntity.ok(response);
        }
        
        long unreadCount = notificationService.getUnreadNotificationsCount(currentUser.get());
        response.put("unreadCount", unreadCount);
        
        return ResponseEntity.ok(response);
    }
} 