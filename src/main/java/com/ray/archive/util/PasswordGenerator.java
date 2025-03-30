package com.ray.archive.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123"; // 你想要设置的新密码
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密密码: " + encodedPassword);
    }
} 