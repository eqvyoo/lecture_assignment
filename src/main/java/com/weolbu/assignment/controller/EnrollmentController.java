package com.weolbu.assignment.controller;

import com.weolbu.assignment.entity.User;
import com.weolbu.assignment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @Operation(summary = "수강 신청", description = "수강생 또는 강사가 강의에 수강 신청을 합니다.")
    public ResponseEntity<String> enrollInLectures(
            @RequestBody List<Long> lectureIds,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 로그인 안된 사용자의 경우, 로그인 요청
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        enrollmentService.enrollInLectures(lectureIds, userDetails);
        return ResponseEntity.ok("수강 신청이 완료되었습니다.");
    }
}