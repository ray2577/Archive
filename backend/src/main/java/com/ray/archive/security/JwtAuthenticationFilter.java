package com.ray.archive.security;

import com.ray.archive.util.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtAuthenticationFilter(UserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        
        logger.debug("处理请求: {}", requestURI);

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            logger.debug("找到JWT令牌: {}", jwt.substring(0, Math.min(10, jwt.length())) + "...");
            try {
                username = jwtTokenUtil.extractUsername(jwt);
                logger.debug("从JWT令牌中提取的用户名: {}", username);
            } catch (Exception e) {
                logger.error("JWT令牌验证失败: {}", e.getMessage());
            }
        } else {
            logger.debug("未找到Authorization头或不是Bearer令牌");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("正在加载用户详情: {}", username);
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                logger.debug("用户详情加载成功: {}, 权限: {}", username, userDetails.getAuthorities());

                if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                    logger.debug("JWT令牌验证成功");
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("已设置认证上下文");
                } else {
                    logger.warn("JWT令牌验证失败");
                }
            } catch (Exception e) {
                logger.error("处理认证时出错: {}", e.getMessage(), e);
            }
        } else {
            if (username == null) {
                logger.debug("没有从令牌中提取到用户名");
            } else {
                logger.debug("认证上下文已存在");
            }
        }
        
        chain.doFilter(request, response);
    }
} 