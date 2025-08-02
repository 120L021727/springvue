package com.example.mdtoword.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 * 
 * 负责JWT Token的生成、验证、解析等操作
 * 使用HMAC-SHA256算法进行签名
 */
@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    // JWT密钥，从配置文件中读取，默认值确保至少256位
    @Value("${jwt.secret:myVeryLongSecretKeyThatIsAtLeast256BitsLong}")
    private String secret;
    
    // Token过期时间（秒），默认24小时
    @Value("${jwt.expiration:86400}")
    private int expiration;
    
    /**
     * 获取签名密钥
     * 
     * @return HMAC-SHA256密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 生成JWT Token
     * 
     * @param username 用户名
     * @return JWT Token字符串
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000L);
        
        return Jwts.builder()
                .subject(username)           // 设置主题（用户名）
                .issuedAt(now)              // 设置签发时间
                .expiration(expiryDate)     // 设置过期时间
                .signWith(getSigningKey())  // 使用密钥签名
                .compact();                 // 生成Token字符串
    }
    
    /**
     * 从Token中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }
    
    /**
     * 验证Token是否有效
     * 
     * @param token JWT Token
     * @return true表示有效，false表示无效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            // 检查Token是否过期
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从Token中获取Claims（声明信息）
     * 
     * @param token JWT Token
     * @return Claims对象
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // 使用密钥验证
                .build()
                .parseSignedClaims(token)     // 解析已签名的Token
                .getPayload();                // 获取载荷部分
    }
    
    /**
     * 获取Token过期时间
     * 
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
    
    /**
     * 检查Token是否过期
     * 
     * @param token JWT Token
     * @return true表示已过期，false表示未过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
} 