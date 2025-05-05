package com.ray.archive.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单诊断控制器
 * 用于系统健康检查和问题诊断
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @Value("${spring.datasource.url:unknown}")
    private String dbUrl;
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 简单健康检查 - 不需要数据库
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("Health check called");
        return ResponseEntity.ok("Service is up and running at " + System.currentTimeMillis());
    }
    
    /**
     * 应用信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        logger.info("Info check called");
        Map<String, Object> info = new HashMap<>();
        info.put("version", "1.0.0");
        info.put("dbConfigured", dbUrl);
        info.put("time", System.currentTimeMillis());
        info.put("hasJdbcTemplate", jdbcTemplate != null);
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * 数据库连接测试
     */
    @GetMapping("/db-check")
    public ResponseEntity<Map<String, Object>> dbCheck() {
        logger.info("Database check called");
        Map<String, Object> result = new HashMap<>();
        result.put("dbUrl", dbUrl);
        
        if (jdbcTemplate == null) {
            result.put("status", "error");
            result.put("message", "JdbcTemplate not available");
            return ResponseEntity.ok(result);
        }
        
        try {
            Integer dbStatus = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            result.put("status", "ok");
            result.put("dbConnected", dbStatus != null && dbStatus == 1);
            result.put("message", "Database connection successful");
        } catch (Exception e) {
            logger.error("Database connection test failed", e);
            result.put("status", "error");
            result.put("message", "Database connection failed: " + e.getMessage());
            result.put("errorType", e.getClass().getName());
        }
        
        return ResponseEntity.ok(result);
    }
} 