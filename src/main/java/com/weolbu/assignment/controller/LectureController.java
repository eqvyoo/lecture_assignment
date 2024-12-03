package com.weolbu.assignment.controller;

import com.weolbu.assignment.dto.LectureCreateRequest;
import com.weolbu.assignment.dto.LectureSearchResponse;
import com.weolbu.assignment.service.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "강의 API", description = "강의 관련 API를 제공합니다.")
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @GetMapping
    @Operation(summary = "강의 조회 기능", description = "모든 회원은 강의를 조회할 수 있습니다.")
    public Page<LectureSearchResponse> getLectures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "popular") String sort
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return lectureService.getLectures(pageRequest, sort);
    }

    @PostMapping
    @Operation(summary = "강의 개설 기능", description = "강사 회원은 강의를 개설할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "강의 생성 성공"),
                    @ApiResponse(responseCode = "403", description = "강사 권한 없음"),
                    @ApiResponse(responseCode = "500", description = "예상치 못한 예외")
            }
    )
    public ResponseEntity<String> createLecture(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LectureCreateRequest lectureRequest
    ) {

        lectureService.createLecture(userDetails, lectureRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("강의가 성공적으로 등록되었습니다.");
    }
}