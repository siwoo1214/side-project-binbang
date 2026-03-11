package com.binbang.backend.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 회원가입 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일을 똑바로 입력하세여")
    private String email;

    @NotBlank(message = "비밀번호도 필수입니다")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$",
            message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    private String password;

    @NotBlank(message = "이름도 필수라고요")
    private String name;

    @NotBlank(message = "얘도요")
    @Pattern(
            regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "올바른 전화번호 형식이 아닙니다"
    )
    private String phone;

}
