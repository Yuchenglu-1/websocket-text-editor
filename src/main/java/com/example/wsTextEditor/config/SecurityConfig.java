package com.example.wsTextEditor.config;

import com.example.wsTextEditor.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;

/**
 * 安全配置类
 * 配置Spring Security的安全策略，包括认证、授权、登录页面等
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤链
     * @param http HttpSecurity对象
     * @return SecurityFilterChain 安全过滤链
     * @throws Exception 异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/api/auth/**", "/login", "/register", "/forgot-password", "/css/**", "/js/**", "/style.css", "/h2-console/**").permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login")
                    .successHandler((request, response, authentication) -> response.sendRedirect("/?loginSuccess=true"))
                    .failureUrl("/login?error")
                    .permitAll()
            )
            .logout(logout ->
                logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            )
            .csrf(csrf -> csrf.disable()) // 临时禁用CSRF以解决403问题
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())); // Allow H2 console frames

        return http.build();
    }

    /**
     * 配置用户详情服务
     * @param userRepository 用户仓库
     * @return UserDetailsService 用户详情服务
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
            .map(user -> {
                // 移除对role字段的依赖，使用默认权限
                return User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities("USER")  // 使用默认权限
                        .build();
            })
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * 配置密码编码器
     * @return PasswordEncoder 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 配置认证管理器
     * @param authConfig 认证配置
     * @return AuthenticationManager 认证管理器
     * @throws Exception 异常
     */
    // 添加AuthenticationManager bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}