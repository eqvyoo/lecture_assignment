package com.weolbu.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weolbu.assignment.controller.AuthController;
import com.weolbu.assignment.dto.LoginRequest;
import com.weolbu.assignment.dto.LoginResponse;
import com.weolbu.assignment.exception.InvalidLoginCredentialException;
import com.weolbu.assignment.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
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
}
