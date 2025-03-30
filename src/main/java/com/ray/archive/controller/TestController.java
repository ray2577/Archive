package com.ray.archive.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试路径映射的控制器
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public Map<String, Object> testRoot() {
        Map<String, Object> response = new HashMap<>();
        response.put("path", "/test");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/api/test")
    public Map<String, Object> testApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("path", "/api/test");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/archive/test")
    public Map<String, Object> testArchive() {
        Map<String, Object> response = new HashMap<>();
        response.put("path", "/archive/test");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/archive/api/test")
    public Map<String, Object> testArchiveApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("path", "/archive/api/test");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
} 