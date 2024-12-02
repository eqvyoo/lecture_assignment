package com.weolbu.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weolbu.assignment.controller.UserController;
import com.weolbu.assignment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
class UserControllerTest {
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    @DisplayName("비밀번호 형식 오류")
    void registerUserInvalidPassword() throws Exception {
        // Given
        String requestBody = """
            {
                "username": "홍길동",
                "email": "test@example.com",
                "phone": "01012345678",
                "password": "abc",
                "role": "STUDENT"
            }
            """;

        // When
        ResultActions result = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );
        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일 형식 오류")
    void registerUserInvalidEmail() throws Exception {
        // Given
        String requestBody = """
                {
                    "username": "홍길동",
                    "email": "invalid-email",
                    "phone": "01012345678",
                    "password": "Abc12345",
                    "role": "STUDENT"
                }
                """;

        // When
        ResultActions result = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        //Then
        result.andExpect(status().isBadRequest());
    }
}
