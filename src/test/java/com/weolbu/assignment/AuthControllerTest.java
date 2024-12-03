package com.weolbu.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weolbu.assignment.config.JwtFilter;
import com.weolbu.assignment.controller.AuthController;
import com.weolbu.assignment.dto.AccessTokenReissueResponse;
import com.weolbu.assignment.dto.LoginRequest;
import com.weolbu.assignment.dto.LoginResponse;
import com.weolbu.assignment.exception.InvalidLoginCredentialException;
import com.weolbu.assignment.exception.InvalidTokenException;
import com.weolbu.assignment.service.AuthService;
import com.weolbu.assignment.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private JwtFilter jwtFilter;
    @MockBean
    private AuthService authService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        // RedisTemplate Mock 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // AuthService Mock 설정
        String mockAccessToken = "mockAccessToken";
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(mockAccessToken));
        when(authService.reissueAccessToken(anyString()))
                .thenReturn(new AccessTokenReissueResponse(mockAccessToken));
    }

    @Nested
    @DisplayName("로그인 성공 테스트")
    class LoginSuccessTests {

        @Test
        @DisplayName("로그인 성공 - Access Token 반환")
        void loginSuccess() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            String accessToken = "generatedAccessToken";

            when(authService.login(any(LoginRequest.class)))
                    .thenReturn(new LoginResponse(accessToken));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(accessToken));
        }
    }

    @Nested
    @DisplayName("로그인 실패 테스트")
    class LoginFailureTests {

        @Test
        @DisplayName("로그인 실패 - 잘못된 이메일")
        void loginInvalidEmail() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("invalid@example.com")
                    .password("password123")
                    .build();

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new InvalidLoginCredentialException("아이디와 비밀번호를 정확히 입력해 주세요."));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("로그인 실패 - 잘못된 비밀번호")
        void loginInvalidPassword() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("wrongPassword")
                    .build();

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new InvalidLoginCredentialException("이메일 또는 비밀번호가 잘못되었습니다. 아이디와 비밀번호를 정확히 입력해 주세요."));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Access Token 재발급 테스트")
    class ReissueAccessTokenTests {

        @Test
        @DisplayName("Access Token 재발급 성공")
        void reissueAccessToken_success() throws Exception {
            // Given
            String expiredToken = "expiredAccessTokenExample";
            String newAccessToken = "newAccessTokenExample";
            AccessTokenReissueResponse response = new AccessTokenReissueResponse(newAccessToken);

            Mockito.when(authService.reissueAccessToken(expiredToken)).thenReturn(response);

            // When
            mockMvc.perform(put("/api/auth/reissue-token")
                            .requestAttr("expiredToken", expiredToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    // Then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(newAccessToken));
        }

        @Nested
        @DisplayName("Access Token 재발급 실패")
        class ReissueAccessTokenFailureTests {

            @Test
            @DisplayName("만료된 Access Token이 누락되었을 때")
            void reissueAccessToken_missingExpiredToken() throws Exception {
                // When
                mockMvc.perform(put("/api/auth/reissue-token")
                                .contentType(MediaType.APPLICATION_JSON))
                        // Then
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("Redis에 Refresh Token이 없을 때")
            void reissueAccessToken_noRefreshTokenInRedis() throws Exception {
                // Given
                String expiredToken = "expiredAccessTokenExample";

                Mockito.when(authService.reissueAccessToken(expiredToken))
                        .thenThrow(new InvalidTokenException("Refresh Token이 Redis에 존재하지 않습니다."));

                // When
                mockMvc.perform(put("/api/auth/reissue-token")
                                .requestAttr("expiredToken", expiredToken)
                                .contentType(MediaType.APPLICATION_JSON))
                        // Then
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("Refresh Token이 만료되었을 때")
            void reissueAccessToken_expiredRefreshToken() throws Exception {
                // Given
                String expiredToken = "expiredAccessTokenExample";

                Mockito.when(authService.reissueAccessToken(expiredToken))
                        .thenThrow(new InvalidTokenException("Refresh Token이 만료되었습니다."));

                // When
                mockMvc.perform(put("/api/auth/reissue-token")
                                .requestAttr("expiredToken", expiredToken)
                                .contentType(MediaType.APPLICATION_JSON))
                        // Then
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("만료된 Access Token 형식이 잘못되었을 때")
            void reissueAccessToken_invalidTokenFormat() throws Exception {
                // Given
                String invalidToken = "invalidTokenExample";

                Mockito.when(authService.reissueAccessToken(invalidToken))
                        .thenThrow(new InvalidTokenException("유효하지 않은 Access Token입니다."));

                // When
                mockMvc.perform(put("/api/auth/reissue-token")
                                .requestAttr("expiredToken", invalidToken)
                                .contentType(MediaType.APPLICATION_JSON))
                        // Then
                        .andExpect(status().isUnauthorized());
            }
        }
    }
}
