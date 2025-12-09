package com.example.wsTextEditor.repository;

import com.example.wsTextEditor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    User findByEmail(String email);
    User findByPhoneNumber(String phoneNumber);

    
    // 通过邀请UUID查找用户
    Optional<User> findByInvitationUuid(String invitationUuid);
}