package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.User;
import com.example.wsTextEditor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // 获取静态资源路径
    @Value("${user.avatar.upload-dir:uploads/avatars/}")
    private String uploadDir;

    // 获取当前用户信息
    @GetMapping
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Fetching profile for user: {}", username);
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            logger.warn("User not found: {}", username);
            return ResponseEntity.status(404).body("User not found");
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phoneNumber", user.getPhoneNumber());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        logger.info("Profile fetched successfully for user: {}", username);
        
        return ResponseEntity.ok(userInfo);
    }

    // 更新用户个人信息
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Updating profile for user: {}", username);
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            logger.warn("User not found when updating profile: {}", username);
            return ResponseEntity.status(404).body("User not found");
        }
        
        // 更新用户信息
        String newEmail = profileData.get("email");
        String newPhoneNumber = profileData.get("phoneNumber");
        
        // 检查邮箱是否已被其他用户使用
        if (newEmail != null && !newEmail.isEmpty() && !newEmail.equals(user.getEmail())) {
            User existingUser = userRepository.findByEmail(newEmail);
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                logger.warn("Email already exists when updating profile for user {}: {}", username, newEmail);
                return ResponseEntity.badRequest().body("Email already exists");
            }
            user.setEmail(newEmail);
        }
        
        // 检查手机号是否已被其他用户使用
        if (newPhoneNumber != null && !newPhoneNumber.isEmpty() && !newPhoneNumber.equals(user.getPhoneNumber())) {
            User existingUser = userRepository.findByPhoneNumber(newPhoneNumber);
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                logger.warn("Phone number already exists when updating profile for user {}: {}", username, newPhoneNumber);
                return ResponseEntity.badRequest().body("Phone number already exists");
            }
            user.setPhoneNumber(newPhoneNumber);
        }
        
        userRepository.save(user);
        logger.info("Profile updated successfully for user: {}", username);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        return ResponseEntity.ok(response);
    }

    // 修改密码
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Changing password for user: {}", username);
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            logger.warn("User not found when changing password: {}", username);
            return ResponseEntity.status(404).body("User not found");
        }
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        // 验证当前密码
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Incorrect current password for user: {}", username);
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password changed successfully for user: {}", username);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }

    // 上传头像
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar") MultipartFile avatarFile) throws IOException{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Uploading avatar for user: {}", username);
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            logger.warn("User not found when uploading avatar: {}", username);
            return ResponseEntity.status(404).body("User not found");
        }
        
        // 检查文件是否为空
        if (avatarFile.isEmpty()) {
            logger.warn("Empty avatar file uploaded by user: {}", username);
            return ResponseEntity.badRequest().body("Avatar file is empty");
        }
        
        // 生成唯一文件名
        String originalFilename = avatarFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // 保存文件到服务器的静态资源目录
        String staticUploadDir = "src/main/resources/static/" + uploadDir;
        Path uploadPath = Paths.get(staticUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 更新用户头像URL
        String avatarUrl = "/" + uploadDir + uniqueFilename;
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        logger.info("Avatar uploaded successfully for user: {}, file: {}", username, uniqueFilename);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Avatar uploaded successfully");
        response.put("avatarUrl", avatarUrl);
        return ResponseEntity.ok(response);
    }
}