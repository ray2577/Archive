package com.ray.archive.util;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
    
    // 使用更简单的HS256算法，只需要256位密钥
    private static final String DEFAULT_SECRET = "archiveSystemDefaultSecretKey12345678901234567890";

    @Value("${jwt.secret:#{null}}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    public String generateToken(UserDetails userDetails) {
        try {
            logger.info("正在为用户 {} 生成JWT令牌", userDetails.getUsername());
            Map<String, Object> claims = new HashMap<>();
            return createToken(claims, userDetails.getUsername());
        } catch (Exception e) {
            logger.error("生成JWT token失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成JWT token失败", e);
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        try {
            // 使用一个确定有效的密钥
            String secretToUse = (secret == null || secret.trim().isEmpty()) ? DEFAULT_SECRET : secret;
            byte[] keyBytes = secretToUse.getBytes(StandardCharsets.UTF_8);
            
            logger.debug("使用HS256算法创建令牌, 密钥长度: {} 字节", keyBytes.length);
            
            // 使用HS256算法而不是HS512，降低密钥长度要求
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(SignatureAlgorithm.HS256, keyBytes)  // 使用HS256
                    .compact();
        } catch (Exception e) {
            logger.error("创建JWT token失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建JWT token失败", e);
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("验证JWT token失败: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            String secretToUse = (secret == null || secret.trim().isEmpty()) ? DEFAULT_SECRET : secret;
            byte[] keyBytes = secretToUse.getBytes(StandardCharsets.UTF_8);
            
            return Jwts.parser()
                    .setSigningKey(keyBytes)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("解析JWT token失败: {}", e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            logger.error("检查JWT token过期失败: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * 获取密钥长度，仅用于日志记录
     */
    public static byte[] getKeyLengthForLogging() {
        return DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8);
    }
} 