package com.weolbu.assignment.service;

import com.weolbu.assignment.dto.LoginRequest;
import com.weolbu.assignment.dto.LoginResponse;
import com.weolbu.assignment.entity.User;
import com.weolbu.assignment.exception.InvalidLoginCredentialException;
import com.weolbu.assignment.repository.UserRepository;
import com.weolbu.assignment.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    // todo : 로그인
    public LoginResponse login(LoginRequest request) {
        // 사용자 인증
        User user = authenticateUser(request);

        // Access Token 및 Refresh Token 생성
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        // Refresh Token Redis에 저장
        saveRefreshTokenToRedis(user.getUserId(), refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }

    // 사용자 인증
    private User authenticateUser(LoginRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        // 입력한 이메일로 가입한 사용자가 없거나, 이메일과 비밀번호가 일치하지 않을 때 동일한 메세지 반환
        if (optionalUser.isEmpty() || !passwordEncoder.matches(request.getPassword(), optionalUser.get().getPassword())) {
            throw new InvalidLoginCredentialException("이메일 또는 비밀번호가 잘못되었습니다. 아이디와 비밀번호를 정확히 입력해 주세요.");
        }

        return optionalUser.get();
    }

    // Access Token 생성
    private String generateAccessToken(User user) {
        return jwtUtil.generateAccessToken(user.getUserId(), user.getRole().name());
    }

    // Refresh Token 생성
    private String generateRefreshToken(User user) {
        return jwtUtil.generateRefreshToken(user.getUserId());
    }

    // Refresh Token을 Redis에 저장
    private void saveRefreshTokenToRedis(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                String.valueOf(userId),
                refreshToken,
                jwtUtil.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );
    }
    // todo : 토큰 재발급

    // todo : 로그아웃
}
