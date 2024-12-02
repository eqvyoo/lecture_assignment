package com.weolbu.assignment;

import com.weolbu.assignment.dto.UserRegisterRequest;
import com.weolbu.assignment.entity.Role;
import com.weolbu.assignment.entity.User;
import com.weolbu.assignment.exception.EmailAlreadyExistsException;
import com.weolbu.assignment.exception.PhoneAlreadyExistsException;
import com.weolbu.assignment.repository.UserRepository;
import com.weolbu.assignment.service.UserService;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("회원가입 성공 테스트")
    class SuccessTests {

        @Test
        @DisplayName("회원가입 성공")
        void registerUserSuccess() {
            // Given

            UserRegisterRequest request = UserRegisterRequest.builder()
                    .username("김철수")
                    .email("cheolsoo@example.com")
                    .phone("01011112222")
                    .password("ABCdef123")
                    .role("Student")
                    .build();

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(userRepository.existsByPhone(request.getPhone())).thenReturn(false);
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

            // When
            userService.registerUser(request);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            Mockito.verify(userRepository, times(1)).save(userCaptor.capture());

            User savedUser = userCaptor.getValue(); // 실제 저장된 객체 확인
            Assertions.assertEquals("김철수", savedUser.getUserName());
            Assertions.assertEquals("cheolsoo@example.com", savedUser.getEmail());
            Assertions.assertEquals("01011112222", savedUser.getPhone());
            Assertions.assertEquals("encodedPassword", savedUser.getPassword());
            Assertions.assertEquals(Role.STUDENT, savedUser.getRole());
        }
    }

    @Nested
    @DisplayName("회원가입 실패 테스트")
    class FailureTests {

        @Test
        @DisplayName("이메일 중복 예외")
        void registerUserEmailDuplicate() {
            // Given
            UserRegisterRequest request = UserRegisterRequest.builder()
                    .username("홍길동")
                    .email("duplicate@example.com")
                    .phone("01012345678")
                    .password("Abc12345")
                    .role("STUDENT")
                    .build();

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            // When & Then
            Assertions.assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(request));
        }

        @Test
        @DisplayName("전화번호 중복 예외")
        void registerUserPhoneDuplicate() {
            // Given

            UserRegisterRequest request = UserRegisterRequest.builder()
                    .username("홍길동")
                    .email("test@example.com")
                    .phone("01011112222")
                    .password("Abc12345")
                    .role("STUDENT")
                    .build();

            when(userRepository.existsByPhone(request.getPhone())).thenReturn(true);

            // When & Then
            Assertions.assertThrows(PhoneAlreadyExistsException.class, () -> userService.registerUser(request));
        }

    }

}

