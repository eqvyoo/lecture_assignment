package com.weolbu.assignment.controller;

import com.weolbu.assignment.entity.User;
import com.weolbu.assignment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(
            summary = "수강 신청",
            description = """
                      사용자가 하나 이상의 강의에 수강 신청을 합니다. 
                      수강생 및 강사 모두 이 API를 호출할 수 있습니다.
                      """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "수강 신청 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "로그인이 필요한 인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음"),
                    @ApiResponse(responseCode = "409", description = "수강 정원 초과로 수강 불가"),
                    @ApiResponse(responseCode = "500", description = "예상치 못한 예외")
            }
    )
    @PostMapping
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