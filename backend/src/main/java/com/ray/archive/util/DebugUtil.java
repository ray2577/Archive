package com.ray.archive.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 调试工具类，用于手动测试JWT token生成
 */
public class DebugUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(DebugUtil.class);
    
    // 使用更简单的HS256算法，只需要256位密钥
    private static final String DEFAULT_SECRET = "archiveSystemDefaultSecretKey12345678901234567890";
    private static final long DEFAULT_EXPIRATION = 86400000; // 24小时
    
    /**
     * 手动生成JWT token测试方法
     */
    public static String generateDebugToken(String username) {
        try {
            logger.info("开始手动生成测试JWT token，用户名: {}", username);
            
            // 创建模拟UserDetails
            UserDetails userDetails = User.withUsername(username)
                    .password("password")
                    .authorities(new ArrayList<>())
                    .build();
            
            // 创建声明
            Map<String, Object> claims = new HashMap<>();
            
            // 使用默认秘钥创建token
            byte[] keyBytes = DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8);
            // 打印实际的密钥长度
            logger.info("测试JWT密钥长度: {} 字节 ({} 位)", keyBytes.length, keyBytes.length * 8);
            
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + DEFAULT_EXPIRATION))
                    .signWith(SignatureAlgorithm.HS256, keyBytes)  // 使用HS256算法
                    .compact();
            
            logger.info("成功生成测试JWT token: {}", token);
            return token;
        } catch (Exception e) {
            logger.error("生成测试JWT token失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成测试JWT token失败", e);
        }
    }
    
    /**
     * 验证生成的JWT token
     */
    public static boolean validateDebugToken(String token, String username) {
        try {
            logger.info("开始验证测试JWT token");
            
            // 解析token
            byte[] keyBytes = DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8);
            String extractedUsername = Jwts.parser()
                    .setSigningKey(keyBytes)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            
            boolean isValid = extractedUsername.equals(username);
            logger.info("测试JWT token验证结果: {}", isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("验证测试JWT token失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取密钥长度（字节数）
     */
    public static int getSecretLength() {
        return DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8).length;
    }
} 