package com.example.studentmanagementsystem.controller;

import com.example.studentmanagementsystem.entity.User;
import com.example.studentmanagementsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
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
        
        // 将验证码存入session
        HttpSession session = request.getSession();
        session.setAttribute("captcha", captcha);
        
        // 将验证码传递给页面
        model.addAttribute("captcha", captcha);
        
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
        
        // 将验证码存入session
        HttpSession session = request.getSession();
        session.setAttribute("captcha", captcha);
        
        // 返回新的验证码
        Map<String, String> response = new HashMap<>();
        response.put("captcha", captcha);
        
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
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, 
                              @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                              Model model) {
        // 1. 验证用户名
        if (!USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            model.addAttribute("errorMessage", "用户名长度必须在3~15位之间，只能是字母加数字的组合，但不能是纯数字");
            return "register";
        }
        
        // 验证用户名唯一性
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("errorMessage", "用户名已存在，请选择其他用户名");
            return "register";
        }
        
        // 2. 验证密码
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            model.addAttribute("errorMessage", "密码长度至少为6个字符");
            return "register";
        }
        
        // 验证两次密码输入一致
        if (confirmPassword == null || !confirmPassword.equals(user.getPassword())) {
            model.addAttribute("errorMessage", "两次输入的密码不一致");
            return "register";
        }
        
        // 3. 验证邮箱
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            model.addAttribute("errorMessage", "请输入有效的邮箱地址");
            return "register";
        }
        
        // 验证邮箱唯一性
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("errorMessage", "邮箱已被注册，请使用其他邮箱");
            return "register";
        }
        
        // 4. 验证身份证号码
        if (user.getIdCard() == null || !ID_CARD_PATTERN.matcher(user.getIdCard()).matches()) {
            model.addAttribute("errorMessage", "请输入有效的18位身份证号码：不能以0开头，前17位必须都是数字，最后一位可以是数字或X");
            return "register";
        }
        
        // 验证身份证唯一性
        if (userService.existsByIdCard(user.getIdCard())) {
            model.addAttribute("errorMessage", "该身份证号已被注册");
            return "register";
        }
        
        // 5. 验证手机号
        if (user.getPhone() == null || !PHONE_PATTERN.matcher(user.getPhone()).matches()) {
            model.addAttribute("errorMessage", "请输入有效的11位手机号码：不能以0开头，必须都是数字");
            return "register";
        }

        // 设置默认角色为STUDENT
        user.setRoles(new HashSet<>());
        user.getRoles().add("ROLE_STUDENT");
        user.setEnabled(true);
        
        userService.saveUser(user);
        return "redirect:/login?registered";
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