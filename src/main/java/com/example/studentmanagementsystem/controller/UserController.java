package com.example.studentmanagementsystem.controller;

import com.example.studentmanagementsystem.entity.User;
import com.example.studentmanagementsystem.service.NotificationService;
import com.example.studentmanagementsystem.service.SystemLogService;
import com.example.studentmanagementsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final SystemLogService systemLogService;

    @Autowired
    public UserController(UserService userService, NotificationService notificationService, 
                         SystemLogService systemLogService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.systemLogService = systemLogService;
    }

    // 获取当前登录用户
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymousUser";
    }
    
    // 检查是否为管理员
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    
    // 操作前权限检查
    private void checkAdminPermission() {
        if (!isAdmin()) {
            throw new AccessDeniedException("只有管理员可以执行此操作");
        }
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        // 获取当前登录用户信息并展示
        String username = getCurrentUsername();
        if (username.equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        Optional<User> currentUser = userService.findByUsername(username);
        if (currentUser.isPresent()) {
            model.addAttribute("user", currentUser.get());
            return "user/profile";
        } else {
            return "redirect:/login";
        }
    }

    // 移除管理员限制，允许所有登录用户查看用户列表
    @GetMapping("/list")
    public String listUsers(Model model, HttpServletRequest request) {
        // 记录系统日志
        systemLogService.logOperation(
            "LIST_USERS", 
            "用户查看用户列表", 
            getCurrentUsername(),
            "SUCCESS", 
            request.getRemoteAddr()
        );
        
        model.addAttribute("users", userService.getAllUsers());
        return "user/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/register")
    public String showRegisterForm(Model model, HttpSession session, 
                                  @RequestParam(required = false) String formKey,
                                  HttpServletRequest request) {
        // 管理员权限检查
        checkAdminPermission();
        
        // 记录系统日志
        systemLogService.logOperation(
            "SHOW_REGISTER_FORM", 
            "管理员访问用户注册页面", 
            getCurrentUsername(),
            "SUCCESS", 
            request.getRemoteAddr()
        );
        
        // 如果没有提供表单键，生成一个新的
        if (formKey == null || formKey.isEmpty()) {
            formKey = UUID.randomUUID().toString().substring(0, 8);
        }
        
        // 存储表单键到模型，用于防止浏览器缓存
        model.addAttribute("formKey", formKey);
        
        // 获取会话中的用户数据（如果有）
        User user = (User) session.getAttribute("adminTempUserData");
        if (user == null) {
            user = new User();
        }
        
        model.addAttribute("user", user);
        
        // 获取和清除会话中可能存在的错误标记
        Map<String, String> errorFields = getErrorFieldsFromSession(session);
        for (Map.Entry<String, String> entry : errorFields.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }
        
        return "user/register";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model, 
                              @RequestParam(value = "formKey", required = false) String formKey,
                              RedirectAttributes redirectAttributes, HttpSession session,
                              HttpServletRequest request) {
        // 管理员权限检查
        checkAdminPermission();
        
        // 生成新的表单键，确保重定向到新的URL
        String newFormKey = UUID.randomUUID().toString().substring(0, 8);
        
        // 保存用户数据到会话，以便在重定向后恢复
        session.setAttribute("adminTempUserData", user);
        
        // 清除所有之前的错误标记
        clearAllErrorMarkersFromSession(session);
        
        // 验证用户名唯一性
        if (userService.existsByUsername(user.getUsername())) {
            markFieldAsError(session, "usernameError", "true");
            addErrorMessage(redirectAttributes, "用户名已存在，请使用其他用户名");
            
            // 记录系统日志
            systemLogService.logOperation(
                "REGISTER_USER", 
                "管理员注册用户失败：用户名已存在", 
                getCurrentUsername(),
                "FAILED", 
                request.getRemoteAddr()
            );
            
            // 清除username字段
            user.setUsername("");
            session.setAttribute("adminTempUserData", user);
            
            return redirectToRegisterWithFormKey(newFormKey);
        }
        
        // 验证邮箱唯一性
        if (userService.existsByEmail(user.getEmail())) {
            markFieldAsError(session, "emailError", "true");
            addErrorMessage(redirectAttributes, "邮箱已被注册，请使用其他邮箱");
            
            // 记录系统日志
            systemLogService.logOperation(
                "REGISTER_USER", 
                "管理员注册用户失败：邮箱已存在", 
                getCurrentUsername(),
                "FAILED", 
                request.getRemoteAddr()
            );
            
            // 清除email字段
            user.setEmail("");
            session.setAttribute("adminTempUserData", user);
            
            return redirectToRegisterWithFormKey(newFormKey);
        }
        
        // 验证身份证号码唯一性
        if (userService.existsByIdCard(user.getIdCard())) {
            markFieldAsError(session, "idCardError", "true");
            addErrorMessage(redirectAttributes, "身份证号码已被注册，请核对后重试");
            
            // 记录系统日志
            systemLogService.logOperation(
                "REGISTER_USER", 
                "管理员注册用户失败：身份证号码已存在", 
                getCurrentUsername(),
                "FAILED", 
                request.getRemoteAddr()
            );
            
            // 清除idCard字段
            user.setIdCard("");
            session.setAttribute("adminTempUserData", user);
            
            return redirectToRegisterWithFormKey(newFormKey);
        }
        
        // 验证手机号唯一性
        if (userService.existsByPhone(user.getPhone())) {
            markFieldAsError(session, "phoneError", "true");
            addErrorMessage(redirectAttributes, "该手机号已被注册，请使用其他手机号");
            
            // 记录系统日志
            systemLogService.logOperation(
                "REGISTER_USER", 
                "管理员注册用户失败：手机号已存在", 
                getCurrentUsername(),
                "FAILED", 
                request.getRemoteAddr()
            );
            
            // 清除phone字段
            user.setPhone("");
            session.setAttribute("adminTempUserData", user);
            
            return redirectToRegisterWithFormKey(newFormKey);
        }
        
        // 设置默认角色为STUDENT，如果未设置
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(new HashSet<>());
            user.getRoles().add("ROLE_STUDENT");
        }
        
        // 设置用户状态为启用
        user.setEnabled(true);
        
        try {
            // 保存用户
            User savedUser = userService.saveUser(user);
            
            // 记录系统日志
            systemLogService.logOperation(
                "REGISTER_USER", 
                "管理员成功注册用户：" + user.getUsername(), 
                getCurrentUsername(),
                "SUCCESS", 
                request.getRemoteAddr()
            );
            
            // 发送注册成功通知给新用户
            notificationService.createNotification(
                "账户创建成功", 
                "您的账户已由管理员 " + getCurrentUsername() + " 创建。欢迎使用学生管理系统！", 
                getCurrentUsername(), 
                savedUser, 
                "USER", 
                savedUser.getId()
            );
            
            // 清除会话中的临时数据
            session.removeAttribute("adminTempUserData");
            clearAllErrorMarkersFromSession(session);
            
            redirectAttributes.addFlashAttribute("message", "用户创建成功");
            return "redirect:/users/list";
        } catch (Exception e) {
            // 捕获可能的其他异常
            addErrorMessage(redirectAttributes, "保存用户时出错: " + e.getMessage());
            
            // 记录系统日志
            systemLogService.logOperation(
                "REGISTER_USER", 
                "管理员注册用户时发生错误: " + e.getMessage(), 
                getCurrentUsername(),
                "ERROR", 
                request.getRemoteAddr()
            );
            
            return redirectToRegisterWithFormKey(newFormKey);
        }
    }
    
    /**
     * 将错误消息添加到重定向属性中
     */
    private void addErrorMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
    }
    
    /**
     * 标记字段为错误状态
     */
    private void markFieldAsError(HttpSession session, String fieldName, String value) {
        session.setAttribute(fieldName, value);
    }
    
    /**
     * 从会话中获取所有错误字段标记
     */
    private Map<String, String> getErrorFieldsFromSession(HttpSession session) {
        Map<String, String> errorFields = new HashMap<>();
        
        // 检查各个可能的错误字段
        String[] fieldNames = {"usernameError", "emailError", "idCardError", "phoneError"};
        for (String fieldName : fieldNames) {
            Object value = session.getAttribute(fieldName);
            if (value != null) {
                errorFields.put(fieldName, value.toString());
                session.removeAttribute(fieldName);
            }
        }
        
        return errorFields;
    }
    
    /**
     * 清除会话中所有错误标记
     */
    private void clearAllErrorMarkersFromSession(HttpSession session) {
        String[] fieldNames = {"usernameError", "emailError", "idCardError", "phoneError", 
                              "adminClearUsername", "adminClearEmail", "adminClearIdCard", "adminClearPhone"};
        for (String fieldName : fieldNames) {
            session.removeAttribute(fieldName);
        }
    }
    
    /**
     * 重定向到注册页面，带上表单键
     */
    private String redirectToRegisterWithFormKey(String formKey) {
        return "redirect:/users/register?formKey=" + formKey;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // 管理员权限检查
        checkAdminPermission();
        
        // 获取用户信息用于通知和记录
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "用户不存在");
            return "redirect:/users/list";
        }
        
        User user = userOpt.get();
        String username = user.getUsername();
        
        try {
            // 发送通知给所有有关用户
            String message = "用户 " + username + " 已被管理员 " + getCurrentUsername() + " 删除";
            notificationService.createNotificationsForAllStudents(
                "用户删除通知", 
                message, 
                getCurrentUsername(), 
                "USER", 
                id
            );
            
            // 删除用户
            userService.deleteUser(id);
            
            // 记录系统日志
            systemLogService.logOperation(
                "DELETE_USER", 
                "管理员删除用户：" + username, 
                getCurrentUsername(),
                "SUCCESS", 
                request.getRemoteAddr()
            );
            
            redirectAttributes.addFlashAttribute("message", "用户已成功删除");
        } catch (Exception e) {
            // 记录系统日志
            systemLogService.logOperation(
                "DELETE_USER", 
                "管理员删除用户失败：" + e.getMessage(), 
                getCurrentUsername(),
                "ERROR", 
                request.getRemoteAddr()
            );
            
            redirectAttributes.addFlashAttribute("errorMessage", "删除用户失败：" + e.getMessage());
        }
        
        return "redirect:/users/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session,
                         @RequestParam(required = false) String v,
                         HttpServletRequest request) {
        // 管理员权限检查
        checkAdminPermission();
        
        // 获取要编辑的用户
        Optional<User> userOptional = userService.getUserById(id);
        
        if (userOptional.isEmpty()) {
            // 记录系统日志
            systemLogService.logOperation(
                "SHOW_EDIT_FORM", 
                "管理员尝试编辑不存在的用户ID: " + id, 
                getCurrentUsername(),
                "FAILED", 
                request.getRemoteAddr()
            );
            
            return "redirect:/users/list";
        }
        
        // 记录系统日志
        systemLogService.logOperation(
            "SHOW_EDIT_FORM", 
            "管理员查看用户编辑表单：" + userOptional.get().getUsername(), 
            getCurrentUsername(),
            "SUCCESS", 
            request.getRemoteAddr()
        );
        
        // 如果没有提供表单键，生成一个新的
        String formKey = v;
        if (formKey == null || formKey.isEmpty()) {
            formKey = UUID.randomUUID().toString().substring(0, 8);
        }
        
        // 存储表单键到模型
        model.addAttribute("formKey", formKey);
        
        // 检查会话中是否有临时用户数据（之前编辑操作的数据）
        User sessionUser = (User) session.getAttribute("editTempUserData");
        User user = sessionUser != null ? sessionUser : userOptional.get();
        
        model.addAttribute("user", user);
        
        // 获取和清除会话中可能存在的错误标记
        Map<String, String> errorFields = getErrorFieldsFromSession(session);
        for (Map.Entry<String, String> entry : errorFields.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }
        
        return "user/edit";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user,
                        @RequestParam(value = "v", required = false) String v,
                        Model model, HttpSession session, RedirectAttributes redirectAttributes,
                        HttpServletRequest request) {
        // 管理员权限检查
        checkAdminPermission();
        
        // 获取原始用户数据
        Optional<User> originalUserOpt = userService.getUserById(id);
        if (originalUserOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "用户不存在");
            
            // 记录系统日志
            systemLogService.logOperation(
                "UPDATE_USER", 
                "管理员尝试更新不存在的用户ID: " + id, 
                getCurrentUsername(),
                "FAILED", 
                request.getRemoteAddr()
            );
            
            return "redirect:/users/list";
        }
        
        User originalUser = originalUserOpt.get();
        
        // 生成新的表单键，确保重定向到新的URL
        String newFormKey = UUID.randomUUID().toString().substring(0, 8);
        
        // 保存表单数据到会话
        session.setAttribute("editTempUserData", user);
        
        // 清除所有之前的错误标记
        clearAllErrorMarkersFromSession(session);
        
        // 验证用户名唯一性 (仅当用户名已更改时)
        if (!user.getUsername().equals(originalUser.getUsername()) && userService.existsByUsername(user.getUsername())) {
            markFieldAsError(session, "usernameError", "true");
            addErrorMessage(redirectAttributes, "用户名已存在，请使用其他用户名");
            
            // 记录系统日志
            systemLogService.logOperation(
                "UPDATE_USER", 
                "管理员更新用户失败：新用户名已存在", 
                getCurrentUsername(),
                "FAILED", 
                request.getRemoteAddr()
            );
            
            user.setUsername("");
            return redirectToEditWithFormKey(id, newFormKey);
        }
        
        // 验证邮箱唯一性 (仅当邮箱已更改时)
        if (!user.getEmail().equals(originalUser.getEmail()) && userService.existsByEmail(user.getEmail())) {
            markFieldAsError(session, "emailError", "true");
            addErrorMessage(redirectAttributes, "邮箱已被注册，请使用其他邮箱");
            
            // 记录系统日志
            systemLogService.logOperation(
                "UPDATE_USER", 
                "管理员更新用户失败：新邮箱已存在", 
                getCurrentUsername(),
                "FAILED", 
                request.getRemoteAddr()
            );
            
            user.setEmail("");
            return redirectToEditWithFormKey(id, newFormKey);
        }
        
        // 验证身份证号码唯一性 (仅当身份证已更改时)
        if (!user.getIdCard().equals(originalUser.getIdCard()) && userService.existsByIdCard(user.getIdCard())) {
            markFieldAsError(session, "idCardError", "true");
            addErrorMessage(redirectAttributes, "身份证号码已被注册，请核对后重试");
            
            // 记录系统日志
            systemLogService.logOperation(
                "UPDATE_USER", 
                "管理员更新用户失败：新身份证号码已存在", 
                getCurrentUsername(),
                "FAILED", 
                request.getRemoteAddr()
            );
            
            user.setIdCard("");
            return redirectToEditWithFormKey(id, newFormKey);
        }
        
        // 验证手机号唯一性 (仅当手机号已更改时)
        if (!user.getPhone().equals(originalUser.getPhone()) && userService.existsByPhone(user.getPhone())) {
            markFieldAsError(session, "phoneError", "true");
            addErrorMessage(redirectAttributes, "该手机号已被注册，请使用其他手机号");
            
            // 记录系统日志
            systemLogService.logOperation(
                "UPDATE_USER", 
                "管理员更新用户失败：新手机号已存在", 
                getCurrentUsername(),
                "FAILED", 
                request.getRemoteAddr()
            );
            
            user.setPhone("");
            return redirectToEditWithFormKey(id, newFormKey);
        }
        
        // 保持原始ID
        user.setId(id);
        
        // 保持原始密码，管理员不应在编辑中修改密码
        user.setPassword(originalUser.getPassword());
        
        try {
            // 收集已更改的字段，用于通知消息
            List<String> changedFields = getChangedFields(originalUser, user);
            
            // 保存更新后的用户
            User updatedUser = userService.updateUser(user);
            
            // 记录系统日志
            systemLogService.logOperation(
                "UPDATE_USER", 
                "管理员成功更新用户：" + user.getUsername() + ", 修改字段: " + String.join(", ", changedFields), 
                getCurrentUsername(),
                "SUCCESS", 
                request.getRemoteAddr()
            );
            
            // 如果有更改，发送通知给用户
            if (!changedFields.isEmpty()) {
                String message = "您的账户信息已由管理员 " + getCurrentUsername() + " 更新。更新的字段包括: " + 
                                String.join(", ", changedFields);
                
                notificationService.createNotification(
                    "账户信息更新通知", 
                    message, 
                    getCurrentUsername(), 
                    updatedUser, 
                    "USER", 
                    updatedUser.getId()
                );
            }
            
            // 清除会话中的临时数据
            session.removeAttribute("editTempUserData");
            clearAllErrorMarkersFromSession(session);
            
            redirectAttributes.addFlashAttribute("message", "用户信息已成功更新");
            return "redirect:/users/list";
        } catch (Exception e) {
            // 捕获可能的其他异常
            addErrorMessage(redirectAttributes, "更新用户时出错: " + e.getMessage());
            
            // 记录系统日志
            systemLogService.logOperation(
                "UPDATE_USER", 
                "管理员更新用户时发生错误: " + e.getMessage(), 
                getCurrentUsername(),
                "ERROR", 
                request.getRemoteAddr()
            );
            
            return redirectToEditWithFormKey(id, newFormKey);
        }
    }

    /**
     * 获取用户更改的字段列表
     */
    private List<String> getChangedFields(User original, User updated) {
        List<String> changedFields = new ArrayList<>();
        
        if (!original.getUsername().equals(updated.getUsername())) {
            changedFields.add("用户名");
        }
        
        if (!original.getFullName().equals(updated.getFullName())) {
            changedFields.add("姓名");
        }
        
        if (!original.getEmail().equals(updated.getEmail())) {
            changedFields.add("邮箱");
        }
        
        if (!original.getIdCard().equals(updated.getIdCard())) {
            changedFields.add("身份证号");
        }
        
        if (!Objects.equals(original.getPhone(), updated.getPhone())) {
            changedFields.add("手机号");
        }
        
        if (!original.getRoles().equals(updated.getRoles())) {
            changedFields.add("用户角色");
        }
        
        if (original.isEnabled() != updated.isEnabled()) {
            changedFields.add("账户状态");
        }
        
        return changedFields;
    }

    /**
     * 重定向到编辑页面，带上表单键
     */
    private String redirectToEditWithFormKey(Long id, String formKey) {
        return "redirect:/users/edit/" + id + "?v=" + formKey;
    }
} 