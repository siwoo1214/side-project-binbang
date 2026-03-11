package com.binbang.backend.member.exception;

import com.binbang.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends CustomException {

    public InvalidPasswordException() {
        super(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}