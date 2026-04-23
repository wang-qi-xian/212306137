package com.stu.helloserver.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 开启 CORS 配置（使用自定义配置源）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 关闭 CSRF 防护（前后端分离项目不需要）
                .csrf(AbstractHttpConfigurer::disable)
                // 设置无状态会话（不创建 session）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 关闭表单登录和 httpBasic 认证
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 自定义未认证请求的返回（返回 401 而非 403）
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或 token 无效\"}");
                        })
                )
                // 配置接口访问规则
                .authorizeHttpRequests(auth -> auth
                        // 放行注册接口：POST /api/users
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        // 放行登录接口：POST /api/users/login
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                        // 其他所有请求都必须认证
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * 配置 CORS 跨域（根据需要调整允许的来源、方法、头等）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://127.0.0.1:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}