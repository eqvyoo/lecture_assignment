package com.weolbu.assignment.controller;

import com.weolbu.assignment.dto.LectureCreateRequest;
import com.weolbu.assignment.service.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "강의 API", description = "강 관련 API를 제공합니다.")
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @PostMapping
    @Operation(summary = "강의 개설 기능", description = "강사 회원은 강의를 개설할 수 있습니다.")
    public ResponseEntity<String> createLecture(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LectureCreateRequest lectureRequest
    ) {

        lectureService.createLecture(userDetails, lectureRequest);

        return ResponseEntity.ok("강의가 성공적으로 등록되었습니다.");
    }
}