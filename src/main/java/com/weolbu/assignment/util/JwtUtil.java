package com.weolbu.assignment.util;

import com.weolbu.assignment.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
@Component
@RequiredArgsConstructor
public class JwtUtil {
    @Value("${spring.jwt.secret-key}")
    private String secret;

    @Value("${spring.jwt.access-token-expiration}")
    private long accessExpTime;

    @Value("${spring.jwt.refresh-token-expiration}")
    private long refreshExpTime;

    private Key key;

    @PostConstruct
    private void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(Long userId, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .claim("tokenType", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpTime))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("tokenType", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpTime))
                .signWith(key)
                .compact();
    }

    public long getRefreshTokenExpiration() {
        return refreshExpTime;
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims parseExpiredToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (Exception e) {
            throw new InvalidTokenException("토큰이 유효하지 않습니다.");
        }
    }
    // 토큰에서 사용자 ID 추출
    public Long getIdFromToken(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }
    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }
    // Access Token 검증
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            if ("access".equals(claims.get("tokenType", String.class)) && !isTokenExpired(token)) {
                return true;
            }
            throw new BadCredentialsException("유효하지 않은 Access Token입니다.");
        } catch (Exception e) {
            throw new BadCredentialsException("유효하지 않은 Access Token입니다.");
        }
    }

    // Refresh Token 검증
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            if ("refresh".equals(claims.get("tokenType", String.class)) && !isTokenExpired(token)) {
                return true;
            }
            throw new BadCredentialsException("유효하지 않은 Refresh Token입니다.");
        } catch (Exception e) {
            throw new BadCredentialsException("다시 로그인 해주세요.");
        }
    }

}
