package com.stu.helloserver.security;

import com.stu.helloserver.mapper.UserMapper;
import com.stu.helloserver.model.entity.User;
import com.stu.helloserver.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 获取 Authorization 头
        String authHeader = request.getHeader("Authorization");

        // 2. 如果没有或不是 Bearer 开头，直接放行（后续过滤器会判断是否需要认证）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 截取 JWT
        String jwt = authHeader.substring(7);
        String username;

        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // Token 解析失败，继续过滤器（不设置认证信息）
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 如果用户名存在且当前 SecurityContext 中还没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 从数据库查询用户（可选：可缓存或使用 UserDetailsService）
            User user = userMapper.selectByUsername(username);  // 需要在 UserMapper 中添加该方法
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 校验 Token 有效性
            if (!jwtUtil.validateToken(jwt, username)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 创建认证令牌（这里没有权限，所以使用空集合；实际项目中可加载用户角色）
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}