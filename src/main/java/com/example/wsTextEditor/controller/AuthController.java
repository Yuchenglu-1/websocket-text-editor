package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.User;
import com.example.wsTextEditor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, HttpServletRequest request) {
        String username = loginRequest.get("username"); // 只能通过用户名登录
        String password = loginRequest.get("password");

        logger.info("User attempting to login: {}", username);
        // 验证用户凭据
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        
        // 设置认证信息
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 将认证信息存储到session中，确保在后续请求中保持登录状态
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        logger.info("User logged in successfully: {}", username);

        // 登录成功，返回成功消息
        Map<String, String> response = new HashMap<>();
        response.put("message", "Login successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");
        String email = registerRequest.get("email");
        String phoneNumber = registerRequest.get("phoneNumber");

        logger.info("New user registration attempt: {}", username);
        // 检查用户名是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            logger.warn("Registration failed: username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists");
        }
        // 检查邮箱是否已存在
        if (email != null && !email.isEmpty() && userRepository.findByEmail(email) != null) {
            logger.warn("Registration failed: email already exists: {}", email);
            throw new IllegalArgumentException("Email already exists");
        }
        // 检查手机号是否已存在
        if (phoneNumber != null && !phoneNumber.isEmpty() && userRepository.findByPhoneNumber(phoneNumber) != null) {
            logger.warn("Registration failed: phone number already exists: {}", phoneNumber);
            throw new IllegalArgumentException("Phone number already exists");
        }
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
        logger.info("User registered successfully: {}", username);

        // 返回成功消息
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }
    
    // 通过邮箱验证用户身份并重置密码
    @PostMapping("/reset-password/email")
    public ResponseEntity<?> resetPasswordByEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        
        logger.info("Password reset attempt via email: {}", email);
        // 检查邮箱是否存在
        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.warn("Password reset failed: email not found: {}", email);
            throw new IllegalArgumentException("Email not found");
        }
        
        // 重置密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password reset successfully for user with email: {}", email);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully via email");
        return ResponseEntity.ok(response);
    }
    
    // 通过手机号验证用户身份并重置密码
    @PostMapping("/reset-password/phone")
    public ResponseEntity<?> resetPasswordByPhone(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String newPassword = request.get("newPassword");
        
        logger.info("Password reset attempt via phone: {}", phoneNumber);
        // 检查手机号是否存在
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            logger.warn("Password reset failed: phone number not found: {}", phoneNumber);
            throw new IllegalArgumentException("Phone number not found");
        }
        
        // 重置密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password reset successfully for user with phone: {}", phoneNumber);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully via phone");
        return ResponseEntity.ok(response);
    }
    
    // 显式的logout端点
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 清除认证信息
        SecurityContextHolder.clearContext();
        
        // 使session失效
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        logger.info("User logged out successfully");
        // 返回成功消息
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Logout successful");
        return ResponseEntity.ok(responseBody);
    }
}