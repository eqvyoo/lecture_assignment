package com.weolbu.assignment;

import com.weolbu.assignment.entity.*;
import com.weolbu.assignment.repository.*;
import com.weolbu.assignment.service.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnrollmentServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User student;
    private Lecture lecture;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock User
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
    }

    @Nested
    @DisplayName("수강 신청 성공 테스트")
    class EnrollmentSuccessTests {

        @Test
        @DisplayName("단일 강의 수강 신청 성공")
        void enrollInSingleLecture_Success() {
            // Given
            UserDetails userDetails = mock(UserDetails.class);
            when(authService.getUserFromUserDetails(userDetails)).thenReturn(student);
            when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));

            // When
            var result = enrollmentService.enrollInLectures(List.of(1L), userDetails);

            // Then
            assertThat(result).containsEntry("Test Lecture", "강의 수강 신청 성공");
            verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
            verify(lectureRepository, times(1)).save(any(Lecture.class));
        }

        @Test
        @DisplayName("여러 강의 수강 신청 성공")
        void enrollInMultipleLectures_Success() {
            // Given
            UserDetails userDetails = mock(UserDetails.class);
            Lecture lecture1 = Lecture.builder().lectureId(1L).title("Lecture 1").currentParticipants(0).maxParticipants(10).build();
            Lecture lecture2 = Lecture.builder().lectureId(2L).title("Lecture 2").currentParticipants(5).maxParticipants(10).build();

            when(authService.getUserFromUserDetails(userDetails)).thenReturn(student);
            when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture1));
            when(lectureRepository.findById(2L)).thenReturn(Optional.of(lecture2));

            // When
            var result = enrollmentService.enrollInLectures(List.of(1L, 2L), userDetails);

            // Then
            assertThat(result).containsEntry("Lecture 1", "강의 수강 신청 성공");
            assertThat(result).containsEntry("Lecture 2", "강의 수강 신청 성공");
            verify(enrollmentRepository, times(2)).save(any(Enrollment.class));
            verify(lectureRepository, times(2)).save(any(Lecture.class));
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
            when(authService.getUserFromUserDetails(userDetails)).thenReturn(student);
            when(lectureRepository.findById(1L)).thenReturn(Optional.empty());

            // When
            var result = enrollmentService.enrollInLectures(List.of(1L), userDetails);

            // Then
            assertThat(result).containsEntry("1", "번 강의를 찾을 수 없습니다.");
            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("이미 수강 신청한 강의를 다시 신청한 경우")
        void enrollInSingleLecture_Fail_AlreadyEnrolled() {
            // Given
            UserDetails userDetails = mock(UserDetails.class);
            when(authService.getUserFromUserDetails(userDetails)).thenReturn(student);
            when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));
            when(enrollmentRepository.existsByLectureAndStudent(lecture, student)).thenReturn(true);

            // When
            var result = enrollmentService.enrollInLectures(List.of(1L), userDetails);

            // Then
            assertThat(result).containsEntry("Test Lecture", " 강의를 이미 수강 신청했습니다.");
            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }


        @Test
        @DisplayName("수강 정원을 초과한 경우")
        void enrollInSingleLecture_Fail_ExceedCapacity() {
            // Given
            lecture = Lecture.builder().lectureId(1L).title("Test Lecture").currentParticipants(10).maxParticipants(10).build();
            UserDetails userDetails = mock(UserDetails.class);

            when(authService.getUserFromUserDetails(userDetails)).thenReturn(student);
            when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));

            // When
            var result = enrollmentService.enrollInLectures(List.of(1L), userDetails);

            // Then
            assertThat(result).containsEntry("Test Lecture", " 강의는 수강 정원을 초과했습니다.");
            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }
    }
}
