package com.weolbu.assignment.service;

import com.weolbu.assignment.dto.UserRegisterRequest;
import com.weolbu.assignment.entity.Role;
import com.weolbu.assignment.exception.EmailAlreadyExistsException;
import com.weolbu.assignment.exception.PhoneAlreadyExistsException;
import com.weolbu.assignment.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.weolbu.assignment.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //1. 회원 가입 기능

    // 이메일 중복여부 확인
    private void validateEmail(String email){
        if (userRepository.existsByEmail(email)){
            throw new EmailAlreadyExistsException("해당 이메일로 가입한 계정이 존재합니다.");
        }
    }

    // 휴대폰 번호 중복 여부 확인 및 정규화된 번호 반환
    private void validatePhone(String phone) {
        if (userRepository.existsByPhone(phone)) {
            throw new PhoneAlreadyExistsException("해당 전화번호로 가입한 계정이 존재합니다.");
        }
    }

    // User 엔티티 생성
    private User createUserEntity(UserRegisterRequest request) {
        Role role = Role.valueOf(request.getRole().toUpperCase());

        return User.builder()
                .userName(request.getUsername())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
    }

    // 회원 가입 처리
    @Transactional
    public void registerUser(UserRegisterRequest request) {
        validateEmail(request.getEmail());
        validatePhone(request.getPhone());

        User user = createUserEntity(request);
        userRepository.save(user);

    }
}
