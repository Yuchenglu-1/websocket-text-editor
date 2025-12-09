package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.User;
import com.example.wsTextEditor.repository.UserRepository;
import com.example.wsTextEditor.service.WebSocketNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    //进行页面定位
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }
    
    @GetMapping("/profile")
    public String profile(Model model) {
        // 获取当前认证的用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username).orElse(null);
            if (currentUser != null) {
                model.addAttribute("currentUser", currentUser);
            }
        }
        return "profile";
    }
    //进行页面跳转
    @GetMapping("/online-users")
    public String onlineUsers(Model model) {
        // 获取当前认证的用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            userRepository.findByUsername(username).ifPresent(currentUser -> model.addAttribute("currentUser", currentUser));
        }
        return "online-users";
    }
    // 获取在线用户列表
    @ResponseBody
    @GetMapping("/api/users/online")
    public ResponseEntity<?> getOnlineUsers() {
        try {
            // 获取在线用户ID列表
            Set<String> onlineUserIds = webSocketNotificationService.getOnlineUsers();

            // 根据ID获取用户详细信息
            List<Map<String, Object>> onlineUsers = new ArrayList<>();
            for (String userId : onlineUserIds) {
                // 根据用户名查找用户（这里存储的是用户名而不是ID）
                Optional<User> userOpt = userRepository.findByUsername(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("username", user.getUsername());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("phoneNumber", user.getPhoneNumber());
                    userInfo.put("avatarUrl", user.getAvatarUrl());
                    userInfo.put("invitationUuid", user.getInvitationUuid());
                    onlineUsers.add(userInfo);
                }
            }

            return ResponseEntity.ok(onlineUsers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching online users: " + e.getMessage());
        }
    }


}