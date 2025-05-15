package com.example.studentmanagementsystem.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 验证码过滤器
 * 用于验证用户输入的验证码是否正确
 */
public class CaptchaFilter extends OncePerRequestFilter {
    
    private RequestMatcher requiresAuthenticationRequestMatcher;
    private AuthenticationFailureHandler failureHandler;
    
    public CaptchaFilter(String loginProcessingUrl) {
        requiresAuthenticationRequestMatcher = new AntPathRequestMatcher(loginProcessingUrl, "POST");
        failureHandler = new SimpleUrlAuthenticationFailureHandler("/login?error=captcha");
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
            throws ServletException, IOException {
        
        // 只对登录请求进行验证码验证
        if (requiresAuthenticationRequestMatcher.matches(request)) {
            System.out.println("======= 开始处理登录请求的验证码验证 =======");
            try {
                validateCaptcha(request);
            } catch (AuthenticationServiceException e) {
                System.out.println("验证码验证失败: " + e.getMessage());
                failureHandler.onAuthenticationFailure(request, response, e);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    /**
     * 验证码校验
     * @param request HttpServletRequest
     * @throws AuthenticationServiceException 验证失败抛出异常
     */
    private void validateCaptcha(HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // 打印会话ID，验证会话是否一致
        System.out.println("当前会话ID: " + session.getId());
        
        // 获取用户提交的验证码ID
        String captchaId = request.getParameter("captchaId");
        System.out.println("用户提交的验证码ID: " + captchaId);
        
        // 如果验证码ID为空，则无法验证
        if (captchaId == null || captchaId.trim().isEmpty()) {
            System.out.println("错误: 验证码ID为空");
            throw new AuthenticationServiceException("验证码ID无效，请刷新页面重试");
        }
        
        // 使用验证码ID获取session中存储的对应验证码
        String sessionCaptcha = (String) session.getAttribute("captcha_" + captchaId);
        System.out.println("会话中的验证码 (ID=" + captchaId + "): " + sessionCaptcha);
        
        // 获取用户输入的验证码
        String requestCaptcha = request.getParameter("captcha");
        System.out.println("用户输入的验证码: " + requestCaptcha);
        
        // 遍历并打印所有session属性，检查是否有其他相关数据
        System.out.println("当前会话中的所有属性:");
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while(attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            System.out.println("  " + name + " = " + session.getAttribute(name));
        }
        
        // 验证码不存在或已过期
        if (sessionCaptcha == null) {
            System.out.println("错误: 会话中验证码为空");
            throw new AuthenticationServiceException("验证码已过期，请刷新页面重试");
        }
        
        // 验证码输入为空
        if (requestCaptcha == null || requestCaptcha.trim().isEmpty()) {
            System.out.println("错误: 用户未输入验证码");
            throw new AuthenticationServiceException("请输入验证码");
        }
        
        // 验证码不匹配
        if (!sessionCaptcha.equalsIgnoreCase(requestCaptcha)) {
            System.out.println("错误: 验证码不匹配");
            System.out.println("  会话验证码(小写): " + sessionCaptcha.toLowerCase());
            System.out.println("  用户验证码(小写): " + requestCaptcha.toLowerCase());
            throw new AuthenticationServiceException("验证码不正确");
        }
        
        System.out.println("验证码验证通过");
        System.out.println("======= 验证码验证结束 =======");
        
        // 验证通过后，从session中移除特定验证码，防止重复使用
        session.removeAttribute("captcha_" + captchaId);
    }
} 