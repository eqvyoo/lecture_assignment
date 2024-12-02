package com.weolbu.assignment;

import com.weolbu.assignment.dto.LoginRequest;
import com.weolbu.assignment.dto.LoginResponse;
import com.weolbu.assignment.entity.Role;
import com.weolbu.assignment.entity.User;
import com.weolbu.assignment.exception.InvalidLoginCredentialException;
import com.weolbu.assignment.repository.UserRepository;
import com.weolbu.assignment.service.AuthService;
import com.weolbu.assignment.util.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Nested
    @DisplayName("로그인 성공 테스트")
    class LoginSuccessTests {

        @Test
        @DisplayName("로그인 성공 - Access Token 반환")
        void loginSuccess() {
            // Given
            String email = "test@example.com";
            String password = "password123";
            String accessToken = "generatedAccessToken";
            String refreshToken = "generatedRefreshToken";

            User mockUser = User.builder()
                    .userId(1L)
                    .email(email)
                    .password("encodedPassword")
                    .role(Role.INSTRUCTOR)
                    .build();

            LoginRequest request = LoginRequest.builder()
                    .email(email)
                    .password(password)
                    .build();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.matches(password, mockUser.getPassword())).thenReturn(true);
            when(jwtUtil.generateAccessToken(mockUser.getUserId(), mockUser.getRole().name())).thenReturn(accessToken);
            when(jwtUtil.generateRefreshToken(mockUser.getUserId())).thenReturn(refreshToken);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // When
            LoginResponse response = authService.login(request);

            // Then
            Assertions.assertNotNull(response);
            Assertions.assertEquals(accessToken, response.getAccessToken());
            Mockito.verify(redisTemplate, times(1)).opsForValue();
        }
    }

    @Nested
    @DisplayName("로그인 실패 테스트")
    class LoginFailureTests {

        @Test
        @DisplayName("로그인 실패 - 잘못된 이메일")
        void loginInvalidEmail() {
            // Given
            String email = "invalid@example.com";
            String password = "password123";

            LoginRequest request = LoginRequest.builder()
                    .email(email)
                    .password(password)
                    .build();

            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // When & Then
            InvalidLoginCredentialException exception = Assertions.assertThrows(
                    InvalidLoginCredentialException.class,
                    () -> authService.login(request)
            );
            Assertions.assertEquals("이메일 또는 비밀번호가 잘못되었습니다. 아이디와 비밀번호를 정확히 입력해 주세요.", exception.getMessage());
        }

        @Test
        @DisplayName("로그인 실패 - 잘못된 비밀번호")
        void loginInvalidPassword() {
            // Given
            String email = "test@example.com";
            String password = "wrongPassword";

            User mockUser = User.builder()
                    .userId(1L)
                    .email(email)
                    .password("encodedPassword")
                    .role(Role.INSTRUCTOR)
                    .build();

            LoginRequest request = LoginRequest.builder()
                    .email(email)
                    .password(password)
                    .build();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.matches(password, mockUser.getPassword())).thenReturn(false);

            // When & Then
            InvalidLoginCredentialException exception = Assertions.assertThrows(
                    InvalidLoginCredentialException.class,
                    () -> authService.login(request)
            );
            Assertions.assertEquals("이메일 또는 비밀번호가 잘못되었습니다. 아이디와 비밀번호를 정확히 입력해 주세요.", exception.getMessage());
        }
    }
}
