package com.stu.helloserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey12345678901234567890123456}")  // 至少32位
    private String secret;

    @Value("${jwt.expiration:3600000}")  // 默认1小时（毫秒）
    private Long expirationMillis;

    /**
     * 生成签名密钥
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 解析全部 Claims
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * 校验 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * 校验 Token 是否有效（用户名匹配且未过期）
     */
    public boolean validateToken(String token, String username) {
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
    }
}