package com.ray.archive.controller;

import com.ray.archive.entity.User;
import com.ray.archive.service.UserService;
import com.ray.archive.util.DebugUtil;
import com.ray.archive.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证相关控制器
 */
@RestController
@RequestMapping({"/auth", "/api/auth"})
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, 
                        UserService userService, 
                        JwtTokenUtil jwtTokenUtil,
                        PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "认证服务正常运行");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        logger.info("收到登录请求: {}", loginRequest.get("username"));
        logger.info("请求内容: {}", loginRequest);
        
        // 检查输入
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            logger.error("登录失败: 用户名或密码为空");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户名和密码不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            // 验证用户凭证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // 设置认证上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 生成JWT令牌
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 添加详细日志
            logger.info("开始生成JWT令牌，用户凭证验证成功，用户: {}", username);
            // 添加密钥长度日志
            byte[] keyBytes = JwtTokenUtil.getKeyLengthForLogging();
            logger.info("JWT密钥长度: {} 字节 ({} 位)", keyBytes.length, keyBytes.length * 8);
            
            // 生成令牌
            String token = jwtTokenUtil.generateToken(userDetails);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("username", userDetails.getUsername());
            
            // 查询用户信息
            Optional<User> user = userService.findByUsername(username);
            if (user != null) {
                response.put("userId", user.get().getId());
                response.put("roles", userDetails.getAuthorities());
            }
            
            logger.info("用户 {} 登录成功", username);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            logger.error("登录失败: 用户名或密码不正确 - {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户名或密码不正确");
            return ResponseEntity.status(401).body(errorResponse);
        } catch (UsernameNotFoundException e) {
            logger.error("登录失败: 用户不存在 - {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户不存在");
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            logger.error("生成JWT token失败: {}", e.getMessage(), e);
            
            // 详细的错误信息
            String rootCause = "";
            Throwable cause = e.getCause();
            if (cause != null) {
                rootCause = cause.getMessage();
                logger.error("根本原因: {}", rootCause);
            }
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "登录失败: " + e.getMessage());
            errorResponse.put("errorType", e.getClass().getName());
            if (!rootCause.isEmpty()) {
                errorResponse.put("rootCause", rootCause);
            }
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-admin")
    public ResponseEntity<?> checkAdmin() {
        logger.info("检查管理员账户状态");
        boolean adminExists = userService.existsByUsername("admin");
        Map<String, Object> response = new HashMap<>();
        response.put("adminExists", adminExists);
        response.put("timestamp", System.currentTimeMillis());
        
        if (adminExists) {
            try {
                User admin = userService.findByUsername("admin").orElse(null);
                response.put("adminInfo", Map.of(
                    "id", admin.getId(),
                    "enabled", admin.getEnabled(),
                    "roles", admin.getRoles()
                ));
                logger.info("管理员账户存在: {}", admin.getUsername());
            } catch (Exception e) {
                logger.error("获取管理员信息失败: {}", e.getMessage());
            }
        } else {
            logger.warn("管理员账户不存在");
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-admin")
    public ResponseEntity<?> resetAdminPassword() {
        logger.info("尝试重置管理员密码");
        try {
            // 检查管理员账户是否存在
            if (!userService.existsByUsername("admin")) {
                logger.info("创建新的管理员账户");
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@example.com");
                admin.setEnabled(true);
                admin.setRoles(new HashSet<>(Arrays.asList("ADMIN")));
                userService.save(admin);
            } else {
                logger.info("更新现有管理员账户密码");
                User admin = userService.findByUsername("admin").orElseThrow();
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEnabled(true);
                userService.save(admin);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "管理员密码已重置为: admin123");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("重置管理员密码失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("重置管理员密码失败: " + e.getMessage());
        }
    }

    @GetMapping("/test-db")
    public ResponseEntity<?> testDatabaseConnection() {
        logger.info("测试数据库连接");
        try {
            boolean adminExists = userService.existsByUsername("admin");
            Map<String, Object> response = new HashMap<>();
            response.put("dbConnectionStatus", "OK");
            response.put("adminExists", adminExists);
            logger.info("数据库连接测试成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("数据库连接测试失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("数据库连接错误: " + e.getMessage());
        }
    }

    @PostMapping("/login-test")
    public ResponseEntity<?> loginTest(@RequestBody Map<String, String> loginRequest) {
        logger.info("收到测试登录请求: {}", loginRequest.get("username"));
        
        // 检查输入
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            logger.error("登录失败: 用户名或密码为空");
            return ResponseEntity.badRequest().body("用户名和密码不能为空");
        }
        
        try {
            // 检查用户是否存在
            if (!userService.existsByUsername(username)) {
                logger.error("登录失败: 用户 {} 不存在", username);
                return ResponseEntity.badRequest().body("用户不存在");
            }
            
            // 简单返回成功，不生成JWT token
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "测试登录成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("测试登录失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("登录失败: " + e.getMessage());
        }
    }

    @GetMapping("/simple-test")
    public ResponseEntity<?> simpleTest(HttpServletRequest request) {
        logger.info("Simple test endpoint called");
        logger.info("Request URI: {}", request.getRequestURI());
        logger.info("Context Path: {}", request.getContextPath());
        logger.info("Servlet Path: {}", request.getServletPath());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Simple test endpoint working");
        response.put("requestUri", request.getRequestURI());
        response.put("contextPath", request.getContextPath());
        response.put("servletPath", request.getServletPath());
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-jwt")
    public ResponseEntity<?> testJwtGeneration() {
        logger.info("收到测试JWT生成请求");
        
        try {
            // 使用调试工具生成测试token
            String testToken = DebugUtil.generateDebugToken("admin");
            
            // 验证生成的token
            boolean isValid = DebugUtil.validateDebugToken(testToken, "admin");
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "JWT令牌测试成功");
            response.put("token", testToken);
            response.put("isValid", isValid);
            response.put("tokenLength", testToken.length());
            response.put("secretLength", DebugUtil.getSecretLength());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("测试JWT生成失败: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "JWT生成测试失败: " + e.getMessage());
            
            // 添加详细错误信息
            if (e.getCause() != null) {
                errorResponse.put("cause", e.getCause().getMessage());
            }
            
            // 获取更精确的异常类型信息
            errorResponse.put("exceptionType", e.getClass().getName());
            
            // 添加堆栈跟踪的简化版本
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace.length > 0) {
                StackTraceElement element = stackTrace[0];
                errorResponse.put("errorLocation", element.getClassName() + "." + 
                                                  element.getMethodName() + "(" + 
                                                  element.getFileName() + ":" + 
                                                  element.getLineNumber() + ")");
            }
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}