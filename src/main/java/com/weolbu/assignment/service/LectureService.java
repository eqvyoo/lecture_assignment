package com.weolbu.assignment.service;

import com.weolbu.assignment.dto.LectureCreateRequest;
import com.weolbu.assignment.dto.LectureSearchResponse;
import com.weolbu.assignment.entity.Lecture;
import com.weolbu.assignment.entity.Role;
import com.weolbu.assignment.entity.User;
import com.weolbu.assignment.exception.InstructorRoleRequiredException;
import com.weolbu.assignment.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final AuthService authService;
    private final LectureRepository lectureRepository;

    public void createLecture(UserDetails userDetails, LectureCreateRequest lectureRequest) {
        // 인증된 사용자 정보를 통해 User 엔티티 가져오기
        User instructor = authService.getUserFromUserDetails(userDetails);

        // 강사인지 확인
        validateInstructorRole(instructor);

        // 강의 생성 및 저장
        Lecture lecture = buildLecture(lectureRequest, instructor);
        lectureRepository.save(lecture);
    }

    // 강사인 지 확인
    private void validateInstructorRole(User user) {
        if (!user.getRole().equals(Role.INSTRUCTOR)) {
            throw new InstructorRoleRequiredException("강사만 강의를 등록할 수 있습니다.");
        }
    }

    // 강의 생성
    private Lecture buildLecture(LectureCreateRequest lectureRequest, User instructor) {
        return Lecture.builder()
                .title(lectureRequest.getTitle())
                .maxParticipants(lectureRequest.getMaxParticipants())
                .currentParticipants(0)
                .price(lectureRequest.getPrice())
                .instructor(instructor)
                .build();
    }

    // 강의 조회 기능


    public Page<LectureSearchResponse> getLectures(Pageable pageable, String sort) {
        if ("rate".equals(sort)) {
            // 신청률 정렬만 JPQL을 사용하기 때문에 따로 처리
            return getLecturesSortedByRate(pageable);
        }

        // 나머지 정렬
        Sort sorting = getSortByCriteria(sort);
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sorting);
        return lectureRepository.findAll(pageRequest).map(this::convertToDto);
    }

    // 신청률 정렬
    private Page<LectureSearchResponse> getLecturesSortedByRate(Pageable pageable) {
        List<Lecture> lectures = lectureRepository.findAllOrderByRate(pageable);
        return new PageImpl<>(
                lectures.stream().map(this::convertToDto).toList(),
                pageable,
                lectures.size()
        );
    }

    // 신청자 많은 순, 최근 등록 순으로 정렬
    private Sort getSortByCriteria(String sort) {
        switch (sort) {
            case "popular": // 신청자 많은 순
                return Sort.by(Sort.Order.desc("currentParticipants"));
            case "recent": // 최근 등록순
                return Sort.by(Sort.Order.desc("createdAt"));
            default: // 기본값 (신청자 많은 순)
                return Sort.by(Sort.Order.desc("currentParticipants"));
        }
    }

    // DTO로 변환
    private LectureSearchResponse convertToDto(Lecture lecture) {
        return LectureSearchResponse.builder()
                .lectureId(lecture.getLectureId())
                .title(lecture.getTitle())
                .price(lecture.getPrice())
                .instructorName(lecture.getInstructor().getUserName())
                .currentParticipants(lecture.getCurrentParticipants())
                .maxParticipants(lecture.getMaxParticipants())
                .build();
    }
}