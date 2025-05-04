package com.ray.archive.config;

import com.ray.archive.entity.User;
import com.ray.archive.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class AdminInitializer {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Bean
    public CommandLineRunner initializeAdmin() {
        return args -> {
            // 检查是否已存在admin用户
            if (!userRepository.existsByUsername("admin")) {
                // 创建新的admin用户
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123")); // 默认密码
                admin.setEmail("admin@example.com");
                admin.setEnabled(true);
                admin.setRoles(new HashSet<>(Arrays.asList("ADMIN")));
                
                userRepository.save(admin);
                System.out.println("管理员账户已创建，用户名: admin, 密码: admin123");
            }
        };
    }
} 