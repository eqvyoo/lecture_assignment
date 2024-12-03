package com.weolbu.assignment.controller;

import com.weolbu.assignment.dto.AccessTokenReissueResponse;
import com.weolbu.assignment.dto.LoginRequest;
import com.weolbu.assignment.dto.LoginResponse;
import com.weolbu.assignment.exception.InvalidTokenException;
import com.weolbu.assignment.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@Tag(name = "인증 API", description = "인증 관련 API를 제공합니다.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @Operation(
            summary = "로그인",
            description = "로그인과 함께 Access token을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "500", description = "예상치 못한 예외")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Access Token 재발급",
            description = "만료된 Access Token으로 새로운 Access Token을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "500", description = "예상치 못한 예외")
            }
    )
    @PutMapping("/reissue-token")
    public ResponseEntity<AccessTokenReissueResponse> reissueAccessToken(HttpServletRequest request) {
        // 필터에서 설정한 만료된 토큰 가져오기
        String expiredAccessToken = (String) request.getAttribute("expiredToken");
        if (expiredAccessToken == null) {
            throw new InvalidTokenException("만료된 Access Token이 없습니다.");
        }
        AccessTokenReissueResponse response = authService.reissueAccessToken(expiredAccessToken);
        return ResponseEntity.ok(response);
    }
}
