package com.weolbu.assignment.service;

import com.weolbu.assignment.entity.Enrollment;
import com.weolbu.assignment.entity.Lecture;
import com.weolbu.assignment.entity.User;
import com.weolbu.assignment.exception.EnrollmentCapacityExceededException;
import com.weolbu.assignment.exception.LectureNotFoundException;
import com.weolbu.assignment.repository.EnrollmentRepository;
import com.weolbu.assignment.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;

    private final AuthService authService;

    @Transactional
    public void enrollInLectures(List<Long> lectureIds, UserDetails userDetails) {
        User user = authService.getUserFromUserDetails(userDetails);

        for (Long lectureId : lectureIds) {
            enrollInSingleLecture(lectureId, user);
        }
    }

    // 단일 강의 수강 신청
    private void enrollInSingleLecture(Long lectureId, User user) {
        Lecture lecture = getLectureWithReadLock(lectureId);

        validateEnrollmentCapacity(lecture);

        saveEnrollment(lecture, user);

        incrementLectureParticipants(lecture);
    }

   // 비관적 잠금을 이용해 강의 조회
    private Lecture getLectureWithReadLock(Long lectureId) {
        return lectureRepository.findByIdForUpdate(lectureId)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다. ID: " + lectureId));
    }

    // 강의 최대 수강 인원 확인
    private void validateEnrollmentCapacity(Lecture lecture) {
        if (!lecture.canEnroll()) {
            throw new EnrollmentCapacityExceededException(lecture.getTitle() + "강의의 최대 수강 인원을 초과했습니다.");
        }
    }

    // 수강 신청 정보 저장
    private void saveEnrollment(Lecture lecture, User user) {
        Enrollment enrollment = Enrollment.builder()
                .lecture(lecture)
                .student(user)
                .build();
        enrollmentRepository.save(enrollment);
    }

    // 강의 신청자 수 증가
    private void incrementLectureParticipants(Lecture lecture) {
        lecture.incrementParticipants();
        lectureRepository.save(lecture);
    }
}