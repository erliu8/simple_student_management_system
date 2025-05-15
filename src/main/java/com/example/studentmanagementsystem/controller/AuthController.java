package com.example.studentmanagementsystem.controller;

import com.example.studentmanagementsystem.entity.User;
import com.example.studentmanagementsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.regex.Pattern;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // 用户名验证：长度在3~15位之间，只能是字母加数字的组合，但不能是纯数字
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])[a-zA-Z0-9]{3,15}$");
    
    // 身份证号码验证：长度为18位，不能以0开头，前17位必须都是数字，最后一位可以是数字或大小写X
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{16}(\\d|X|x)$");
    
    // 手机号验证：长度为11位，不能以0开头，必须都是数字
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[1-9]\\d{10}$");
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    
    // 验证码长度
    private static final int CAPTCHA_LENGTH = 4;
    
    // 用于生成验证码的字符集
    private static final String CAPTCHA_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        // 生成验证码
        String captcha = generateCaptcha(CAPTCHA_LENGTH);
        
        // 生成唯一的验证码ID
        String captchaId = UUID.randomUUID().toString().substring(0, 8);
        
        // 将验证码存入session，使用ID作为键的一部分
        HttpSession session = request.getSession();
        System.out.println("======= 登录页面加载 =======");
        System.out.println("登录页面会话ID: " + session.getId());
        System.out.println("生成的验证码: " + captcha);
        System.out.println("验证码ID: " + captchaId);
        
        // 使用ID作为前缀存储验证码
        session.setAttribute("captcha_" + captchaId, captcha);
        
        // 将验证码和ID传递给页面
        model.addAttribute("captcha", captcha);
        model.addAttribute("captchaId", captchaId);
        
        return "login";
    }
    
    /**
     * 刷新验证码
     * @param request HttpServletRequest
     * @return 新的验证码
     */
    @GetMapping("/refreshCaptcha")
    @ResponseBody
    public ResponseEntity<Map<String, String>> refreshCaptcha(HttpServletRequest request) {
        // 生成新的验证码
        String captcha = generateCaptcha(CAPTCHA_LENGTH);
        
        // 生成新的验证码ID
        String captchaId = UUID.randomUUID().toString().substring(0, 8);
        
        // 将验证码存入session
        HttpSession session = request.getSession();
        System.out.println("======= 刷新验证码 =======");
        System.out.println("刷新验证码会话ID: " + session.getId());
        System.out.println("刷新后的验证码: " + captcha);
        System.out.println("新验证码ID: " + captchaId);
        
        // 使用ID作为前缀存储验证码
        session.setAttribute("captcha_" + captchaId, captcha);
        
        // 返回新的验证码和ID
        Map<String, String> response = new HashMap<>();
        response.put("captcha", captcha);
        response.put("captchaId", captchaId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 生成指定长度的验证码
     * @param length 验证码长度
     * @return 生成的验证码
     */
    private String generateCaptcha(int length) {
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();
        
        // 确保至少有一个字母和一个数字
        captcha.append(CAPTCHA_CHARS.charAt(random.nextInt(26))); // 添加一个字母
        captcha.append(CAPTCHA_CHARS.charAt(26 + random.nextInt(10))); // 添加一个数字
        
        // 添加剩余的随机字符
        for (int i = 2; i < length; i++) {
            captcha.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        
        // 打乱顺序
        char[] charArray = captcha.toString().toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            int j = random.nextInt(charArray.length);
            char temp = charArray[i];
            charArray[i] = charArray[j];
            charArray[j] = temp;
        }
        
        return new String(charArray);
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpSession session, 
                                      @RequestParam(required = false) String v,
                                      HttpServletResponse response) {
        // 添加缓存控制头，防止浏览器缓存页面
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // 如果没有提供表单键，生成一个新的
        String formKey = v;
        if (formKey == null || formKey.isEmpty()) {
            formKey = UUID.randomUUID().toString().substring(0, 8);
        }
        
        // 存储表单键到模型，用于防止浏览器缓存
        model.addAttribute("formKey", formKey);
        
        // 获取会话中的用户数据（如果有）
        User user = (User) session.getAttribute("tempUserData");
        if (user == null) {
            user = new User();
        }
        
        model.addAttribute("user", user);

        // 获取和清除会话中可能存在的错误标记
        Map<String, String> errorFields = getErrorFieldsFromSession(session);
        for (Map.Entry<String, String> entry : errorFields.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }
        
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, 
                              @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                              @RequestParam(value = "v", required = false) String v,
                              Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            // 生成新的表单键，确保重定向到新的URL
            String newFormKey = UUID.randomUUID().toString().substring(0, 8);
            
            // 保存用户数据到会话，以便在重定向后恢复
            session.setAttribute("tempUserData", user);
            
            // 清除所有之前的错误标记
            clearAllErrorMarkersFromSession(session);
            
            // 创建一个错误列表，收集所有验证错误
            List<String> errors = new ArrayList<>();
            boolean hasError = false;
            int uniquenessErrorCount = 0; // 记录唯一性错误的数量
            
            // 1. 验证用户名
            if (!USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
                errors.add("用户名长度必须在3~15位之间，只能是字母加数字的组合，但不能是纯数字");
                hasError = true;
            } else if (userService.existsByUsername(user.getUsername())) {
                // 验证用户名唯一性
                markFieldAsError(session, "usernameError", "true");
                errors.add("用户名已存在，请选择其他用户名");
                user.setUsername("");
                hasError = true;
                uniquenessErrorCount++;
            }
            
            // 2. 验证密码
            if (user.getPassword() == null || user.getPassword().length() < 6) {
                errors.add("密码长度至少为6个字符");
                hasError = true;
            } else if (confirmPassword == null || !confirmPassword.equals(user.getPassword())) {
                // 验证两次密码输入一致
                errors.add("两次输入的密码不一致");
                hasError = true;
            }
            
            // 3. 验证邮箱
            if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
                errors.add("请输入有效的邮箱地址");
                hasError = true;
            } else if (userService.existsByEmail(user.getEmail())) {
                // 验证邮箱唯一性
                markFieldAsError(session, "emailError", "true");
                errors.add("邮箱已被注册，请使用其他邮箱");
                user.setEmail("");
                hasError = true;
                uniquenessErrorCount++;
            }
            
            // 4. 验证身份证号码
            if (user.getIdCard() == null || !ID_CARD_PATTERN.matcher(user.getIdCard()).matches()) {
                errors.add("请输入有效的18位身份证号码：不能以0开头，前17位必须都是数字，最后一位可以是数字或X");
                hasError = true;
            } else if (userService.existsByIdCard(user.getIdCard())) {
                // 验证身份证唯一性
                markFieldAsError(session, "idCardError", "true");
                errors.add("该身份证号已被注册");
                user.setIdCard("");
                hasError = true;
            }
            
            // 5. 验证手机号
            if (user.getPhone() == null || !PHONE_PATTERN.matcher(user.getPhone()).matches()) {
                errors.add("请输入有效的11位手机号码：不能以0开头，必须都是数字");
                hasError = true;
            } else if (userService.existsByPhone(user.getPhone())) {
                // 验证手机号唯一性
                markFieldAsError(session, "phoneError", "true");
                errors.add("该手机号已被注册，请使用其他手机号");
                user.setPhone("");
                hasError = true;
                uniquenessErrorCount++;
            }
            
            // 如果有任何验证错误，更新会话数据并重定向
            if (hasError) {
                // 更新会话中的临时用户数据，保留已清除错误字段的用户对象
                session.setAttribute("tempUserData", user);
                
                // 如果有多个唯一性错误，添加特殊标记
                if (uniquenessErrorCount > 1) {
                    session.setAttribute("multipleErrors", "true");
                    // 添加一条特别的提示
                    errors.add(0, "注意：系统已标记出所有需要修改的重复字段，请一次性全部修改后再提交");
                }
                
                // 将所有错误组合成一条消息
                String errorMessage = String.join(" | ", errors);
                addErrorMessage(redirectAttributes, errorMessage);
                
                return redirectToRegisterWithFormKey(newFormKey);
            }

            // 设置默认角色为STUDENT
            user.setRoles(new HashSet<>());
            user.getRoles().add("ROLE_STUDENT");
            user.setEnabled(true);
            
            // 所有验证通过，保存用户
            userService.saveUser(user);
            
            // 清除会话中的临时数据
            session.removeAttribute("tempUserData");
            clearAllErrorMarkersFromSession(session);
            
            // 重定向到登录页面，带上注册成功标记
            return "redirect:/login?registered";
        } catch (Exception e) {
            // 捕获可能发生的所有异常
            addErrorMessage(redirectAttributes, "注册过程中发生错误: " + e.getMessage());
            return redirectToRegisterWithFormKey(UUID.randomUUID().toString().substring(0, 8));
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
        String[] fieldNames = {"usernameError", "emailError", "idCardError", "phoneError", "multipleErrors"};
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
                              "clearUsername", "clearEmail", "clearIdCard", "clearPhone", "multipleErrors"};
        for (String fieldName : fieldNames) {
            session.removeAttribute(fieldName);
        }
    }
    
    /**
     * 重定向到注册页面，带上表单键
     */
    private String redirectToRegisterWithFormKey(String formKey) {
        return "redirect:/register?v=" + formKey;
    }
    
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("step", "verify"); // 初始步骤为"验证身份"
        return "forgot-password";
    }
    
    @PostMapping("/forgot-password/verify")
    public String verifyUserIdentity(@RequestParam("username") String username, 
                                    @RequestParam(value = "idCard", required = false) String idCard,
                                    @RequestParam(value = "phone", required = false) String phone,
                                    Model model) {
        // 1. 验证用户名是否存在
        Optional<User> userOptional = userService.findByUsername(username);
        
        if (userOptional.isEmpty()) {
            model.addAttribute("errorMessage", "未注册的用户名");
            model.addAttribute("step", "verify");
            return "forgot-password";
        }
        
        // 如果是第一步，只需要验证用户名
        if (idCard == null || phone == null) {
            model.addAttribute("username", username);
            model.addAttribute("step", "enter-info");
            model.addAttribute("user", userOptional.get());
            return "forgot-password";
        }
        
        // 2. 验证身份证号和手机号
        User user = userOptional.get();
        
        if (!user.getIdCard().equals(idCard)) {
            model.addAttribute("errorMessage", "身份证号码不匹配");
            model.addAttribute("username", username);
            model.addAttribute("step", "enter-info");
            model.addAttribute("user", user);
            return "forgot-password";
        }
        
        if (!user.getPhone().equals(phone)) {
            model.addAttribute("errorMessage", "手机号码不匹配");
            model.addAttribute("username", username);
            model.addAttribute("step", "enter-info");
            model.addAttribute("user", user);
            return "forgot-password";
        }
        
        // 3. 验证通过，进入修改密码阶段
        model.addAttribute("username", username);
        model.addAttribute("step", "reset-password");
        model.addAttribute("user", user);
        return "forgot-password";
    }
    
    @PostMapping("/forgot-password/reset")
    public String resetPassword(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               @RequestParam("confirmPassword") String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        // 1. 获取用户
        Optional<User> userOptional = userService.findByUsername(username);
        
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "用户不存在");
            return "redirect:/forgot-password";
        }
        
        User user = userOptional.get();
        
        // 2. 验证密码
        if (password == null || password.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "密码长度至少为6个字符");
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("step", "reset-password");
            return "redirect:/forgot-password";
        }
        
        // 3. 验证两次密码输入一致
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "两次输入的密码不一致");
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("step", "reset-password");
            return "redirect:/forgot-password";
        }
        
        // 4. 更新密码
        user.setPassword(password);
        userService.saveUser(user);
        
        redirectAttributes.addFlashAttribute("successMessage", "密码修改成功，请使用新密码登录");
        return "redirect:/login";
    }
} 