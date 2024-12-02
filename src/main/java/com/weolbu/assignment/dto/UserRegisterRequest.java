package com.weolbu.assignment.dto;

import com.weolbu.assignment.validation.annotation.SingleRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String username;
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    private String phone;
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[A-Z].*[a-z0-9]|.*[a-z].*[A-Z0-9]|.*[0-9].*[A-Za-z]).{6,10}$",
            message = "비밀번호는 영문 대문자, 소문자, 숫자 중 최소 두 가지를 포함한 6자 이상 10자 이하로 설정해주세요.")
    private String password;
    @NotBlank(message = "회원 유형을 선택해주세요. ")
    @SingleRole
    private String role;

}
