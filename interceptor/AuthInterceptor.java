package com.stu.helloserver.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取 Authorization 令牌
        String token = request.getHeader("Authorization");

        // 如果没有携带令牌，返回 401 错误
        if (token == null || token.isEmpty()) {
            response.setContentType("application/json;charset=UTF-8");
            String errorJson = "{\"code\": 401, \"msg\": \"登录凭证已缺失或过期，请重新登录\"}";
            response.getWriter().write(errorJson);
            return false; // 拦截
        }

        // 令牌存在，放行
        return true;
    }
}