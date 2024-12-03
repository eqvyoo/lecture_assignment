package com.weolbu.assignment;

import com.weolbu.assignment.entity.Enrollment;
import com.weolbu.assignment.entity.Lecture;
import com.weolbu.assignment.entity.Role;
import com.weolbu.assignment.entity.User;
import com.weolbu.assignment.exception.EnrollmentCapacityExceededException;
import com.weolbu.assignment.exception.LectureNotFoundException;
import com.weolbu.assignment.repository.EnrollmentRepository;
import com.weolbu.assignment.repository.LectureRepository;
import com.weolbu.assignment.service.AuthService;
import com.weolbu.assignment.service.EnrollmentService;
import com.weolbu.assignment.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
class EnrollmentServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User student;
    private Lecture lecture;
    private String mockJwt;
    private Claims mockClaims;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock Student
        student = User.builder()
                .userId(1L)
                .userName("Test Student")
                .email("student@example.com")
                .role(Role.STUDENT)
                .build();

        // Mock Lecture
        lecture = Lecture.builder()
                .lectureId(1L)
                .title("Test Lecture")
                .currentParticipants(0)
                .maxParticipants(10)
                .build();

        // Mock JWT
        mockJwt = "mock-jwt-token";

        // Mock Claims
        mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn("1"); // 사용자 ID
        when(mockClaims.get("role", String.class)).thenReturn("STUDENT");
    }

    @Nested
    @DisplayName("강의 수강 신청 성공 테스트")
    class EnrollmentSuccessTests {

        @Test
        @DisplayName("단일 강의 수강 신청 성공")
        void enrollInSingleLecture_Success() {
            // Given
            UserDetails userDetails = mock(UserDetails.class);
            when(jwtUtil.validateAccessToken(mockJwt)).thenReturn(true);
            when(jwtUtil.parseToken(mockJwt)).thenReturn(mockClaims);
            when(authService.getUserFromUserDetails(userDetails)).thenReturn(student);
            when(lectureRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lecture));

            // When
            enrollmentService.enrollInLectures(List.of(1L), userDetails);

            // Then
            verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
            verify(lectureRepository, times(1)).save(any(Lecture.class));
        }

        @Test
        @DisplayName("여러 강의 수강 신청 성공")
        void enrollInMultipleLectures_Success() {
            // Given
            UserDetails userDetails = mock(UserDetails.class);
            Lecture lecture1 = Lecture.builder()
                    .lectureId(1L)
                    .title("Lecture 1")
                    .currentParticipants(0)
                    .maxParticipants(10)
                    .build();
            Lecture lecture2 = Lecture.builder()
                    .lectureId(2L)
                    .title("Lecture 2")
                    .currentParticipants(5)
                    .maxParticipants(10)
                    .build();

            // Mock JWT 유효성 검사 및 Claims 설정
            when(jwtUtil.validateAccessToken(mockJwt)).thenReturn(true);
            when(jwtUtil.parseToken(mockJwt)).thenReturn(mockClaims);

            // 사용자 인증 Mock 설정
            when(authService.getUserFromUserDetails(userDetails)).thenReturn(student);

            // 강의 조회 Mock 설정
            when(lectureRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lecture1));
            when(lectureRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(lecture2));

            // When
            enrollmentService.enrollInLectures(List.of(1L, 2L), userDetails);

            // Then
            verify(enrollmentRepository, times(2)).save(any(Enrollment.class));
            verify(lectureRepository, times(2)).save(any(Lecture.class));
            assertThat(lecture1.getCurrentParticipants()).isEqualTo(1);
            assertThat(lecture2.getCurrentParticipants()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("강의 수강 신청 실패 테스트")
    class EnrollmentFailureTests {

        @Test
        @DisplayName("강의가 존재하지 않는 경우")
        void enrollInSingleLecture_Fail_LectureNotFound() {
            // Given
            UserDetails userDetails = mock(UserDetails.class);
            when(jwtUtil.validateAccessToken(mockJwt)).thenReturn(true);
            when(jwtUtil.parseToken(mockJwt)).thenReturn(mockClaims);
            when(authService.getUserFromUserDetails(userDetails)).thenReturn(student);
            when(lectureRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> enrollmentService.enrollInLectures(List.of(1L), userDetails))
                    .isInstanceOf(LectureNotFoundException.class)
                    .hasMessageContaining("강의를 찾을 수 없습니다. ID: 1");
        }

        @Test
        @DisplayName("강의 최대 수강 인원을 초과한 경우")
        void enrollInSingleLecture_Fail_ExceedCapacity() {
            // Given
            lecture = Lecture.builder()
                    .lectureId(1L)
                    .title("Test Lecture")
                    .currentParticipants(10) // 이미 최대 인원
                    .maxParticipants(10)
                    .build();

            UserDetails userDetails = mock(UserDetails.class);
            when(jwtUtil.validateAccessToken(mockJwt)).thenReturn(true);
            when(jwtUtil.parseToken(mockJwt)).thenReturn(mockClaims);
            when(authService.getUserFromUserDetails(userDetails)).thenReturn(student);
            when(lectureRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lecture));

            // When & Then
            assertThatThrownBy(() -> enrollmentService.enrollInLectures(List.of(1L), userDetails))
                    .isInstanceOf(EnrollmentCapacityExceededException.class)
                    .hasMessageContaining("강의의 최대 수강 인원을 초과했습니다.");
        }
    }
}