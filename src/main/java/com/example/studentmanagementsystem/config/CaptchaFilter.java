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
            try {
                validateCaptcha(request);
            } catch (AuthenticationServiceException e) {
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
        
        // 获取session中存储的验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        
        // 获取用户输入的验证码
        String requestCaptcha = request.getParameter("captcha");
        
        // 验证码不存在或已过期
        if (sessionCaptcha == null) {
            throw new AuthenticationServiceException("验证码已过期，请刷新页面重试");
        }
        
        // 验证码输入为空
        if (requestCaptcha == null || requestCaptcha.trim().isEmpty()) {
            throw new AuthenticationServiceException("请输入验证码");
        }
        
        // 验证码不匹配
        if (!sessionCaptcha.equalsIgnoreCase(requestCaptcha)) {
            throw new AuthenticationServiceException("验证码不正确");
        }
        
        // 验证通过后，从session中移除验证码，防止重复使用
        session.removeAttribute("captcha");
    }
} 