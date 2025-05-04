package com.ray.archive.config;

import com.ray.archive.security.JwtAuthenticationFilter;
import com.ray.archive.service.UserService;
import com.ray.archive.util.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserService userService, JwtTokenUtil jwtTokenUtil, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(userService, jwtTokenUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/auth/health", "/api/auth/health").permitAll()
                .requestMatchers("/auth/login", "/api/auth/login").permitAll()
                .requestMatchers("/auth/login-test", "/api/auth/login-test").permitAll()
                .requestMatchers("/auth/simple-test", "/api/auth/simple-test").permitAll()
                .requestMatchers("/auth/reset-admin", "/api/auth/reset-admin").permitAll()
                .requestMatchers("/auth/check-admin", "/api/auth/check-admin").permitAll()
                .requestMatchers("/auth/test-db", "/api/auth/test-db").permitAll()
                .requestMatchers("/test").permitAll()
                .requestMatchers("/api/test").permitAll()
                .requestMatchers("/archive/test").permitAll()
                .requestMatchers("/archive/api/test").permitAll()
                .anyRequest().permitAll()
                // 以下是原来的配置，现在已注释掉
                // .requestMatchers("/auth/**").permitAll()
                // .requestMatchers("/api/auth/**").permitAll()
                // .requestMatchers("/users/register", "/api/users/register").permitAll()
                // .anyRequest().authenticated()
            );
            // 暂时注释掉 JWT 过滤器
            // .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}