package com.weolbu.assignment;


import com.weolbu.assignment.dto.LectureCreateRequest;
import com.weolbu.assignment.entity.Lecture;
import com.weolbu.assignment.entity.Role;
import com.weolbu.assignment.entity.User;
import com.weolbu.assignment.exception.InstructorRoleRequiredException;
import com.weolbu.assignment.repository.LectureRepository;
import com.weolbu.assignment.service.AuthService;
import com.weolbu.assignment.service.LectureService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private LectureRepository lectureRepository;

    @InjectMocks
    private LectureService lectureService;

    @Nested
    @DisplayName("강의 생성 성공 테스트")
    class CreateLectureSuccessTests {

        private User instructor;

        @BeforeEach
        void setUp() {
            instructor = User.builder()
                    .userId(1L)
                    .userName("강사")
                    .password("password")
                    .email("instructor@example.com")
                    .phone("01045627890")
                    .role(Role.INSTRUCTOR)
                    .build();
        }

        @Test
        @DisplayName("강사가 강의 생성에 성공한다")
        void createLecture_Success() {
            // Given
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername("instructor@example.com")
                    .password("password")
                    .roles("INSTRUCTOR")
                    .build();

            LectureCreateRequest lectureRequest = LectureCreateRequest.builder()
                    .title("강의 제목")
                    .maxParticipants(100)
                    .price(100000)
                    .build();

            when(authService.getUserFromUserDetails(userDetails)).thenReturn(instructor);

            // When
            lectureService.createLecture(userDetails, lectureRequest);

            // Then
            Mockito.verify(lectureRepository, times(1)).save(any(Lecture.class));
        }
    }

    @Nested
    @DisplayName("강의 생성 실패 테스트")
    class CreateLectureFailureTests {

        private User student;

        @BeforeEach
        void setUp() {
            student = User.builder()
                    .userId(2L)
                    .userName("수강생")
                    .password("password")
                    .email("student@example.com")
                    .phone("01045627890")
                    .role(Role.STUDENT)
                    .build();
        }

        @Test
        @DisplayName("수강생이 강의 생성 시도 시 실패한다")
        void createLecture_Failure_StudentRole() {
            // Given
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername("student@example.com")
                    .password("password")
                    .roles("STUDENT")
                    .build();

            LectureCreateRequest lectureRequest = LectureCreateRequest.builder()
                    .title("강의 제목")
                    .maxParticipants(100)
                    .price(100000)
                    .build();

            when(authService.getUserFromUserDetails(userDetails)).thenReturn(student);

            // When & Then
            assertThrows(InstructorRoleRequiredException.class,
                    () -> lectureService.createLecture(userDetails, lectureRequest));

            Mockito.verify(lectureRepository, times(0)).save(any(Lecture.class));
        }
    }
}