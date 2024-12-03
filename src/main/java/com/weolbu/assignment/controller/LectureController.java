package com.weolbu.assignment.controller;

import com.weolbu.assignment.dto.LectureCreateRequest;
import com.weolbu.assignment.service.LectureService;
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
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @PostMapping
    public ResponseEntity<String> createLecture(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LectureCreateRequest lectureRequest
    ) {

        lectureService.createLecture(userDetails, lectureRequest);

        return ResponseEntity.ok("강의가 성공적으로 등록되었습니다.");
    }
}