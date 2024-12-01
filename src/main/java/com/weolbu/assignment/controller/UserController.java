package com.weolbu.assignment.controller;

import com.weolbu.assignment.dto.UserRegisterRequest;
import com.weolbu.assignment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegisterRequest request){
        userService.registerUser(request);
        return ResponseEntity.ok("회원가입을 성공했습니다. ");
    }
}
