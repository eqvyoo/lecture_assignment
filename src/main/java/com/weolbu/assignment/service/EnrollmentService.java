package com.weolbu.assignment.service;

import com.weolbu.assignment.entity.Enrollment;
import com.weolbu.assignment.entity.Lecture;
import com.weolbu.assignment.entity.User;
import com.weolbu.assignment.exception.AlreadyEnrolledException;
import com.weolbu.assignment.exception.EnrollmentCapacityExceededException;
import com.weolbu.assignment.exception.LectureNotFoundException;
import com.weolbu.assignment.repository.EnrollmentRepository;
import com.weolbu.assignment.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;

    private final AuthService authService;

    @Transactional
    public Map<String, String> enrollInLectures(List<Long> lectureIds, UserDetails userDetails) {
        User user = authService.getUserFromUserDetails(userDetails);
        Map<String, String> enrollmentResults = new HashMap<>();

        for (Long lectureId : lectureIds) {
            handleEnrollment(lectureId, user, enrollmentResults);
        }

        return enrollmentResults;
    }

    private void handleEnrollment(Long lectureId, User user, Map<String, String> results) {
        String lectureTitle = null;
        try {
            Lecture lecture = getLecture(lectureId);
            lectureTitle = lecture.getTitle();
            enrollInSingleLecture(lecture, user);
            results.put(lectureTitle, "강의 수강 신청 성공");
        } catch (LectureNotFoundException e) {
            results.put(String.valueOf(lectureId), "번 강의를 찾을 수 없습니다.");
        } catch (EnrollmentCapacityExceededException e) {
            results.put(lectureTitle, " 강의는 수강 정원을 초과했습니다.");
        } catch (AlreadyEnrolledException e) {
            results.put(lectureTitle, " 강의를 이미 수강 신청했습니다.");
        }
    }

    // 강의 조회
    private Lecture getLecture(Long lectureId) {
        return lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException("존재하지 않는 강의입니다."));
    }

    // 단일 강의 수강 신청
    @Transactional
    public void enrollInSingleLecture(Lecture lecture, User user) {
        validateEnrollmentCapacity(lecture);
        validateDuplicateEnrollment(lecture, user);

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

    // 중복 수강 신청 확인
    private void validateDuplicateEnrollment(Lecture lecture, User user) {
        boolean alreadyEnrolled = enrollmentRepository.existsByLectureAndStudent(lecture, user);
        if (alreadyEnrolled) {
            throw new AlreadyEnrolledException("이미 수강 신청한 강의입니다: " + lecture.getTitle());
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