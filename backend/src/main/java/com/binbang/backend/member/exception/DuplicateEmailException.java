package com.binbang.backend.member.exception;

import com.binbang.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends CustomException {
    public DuplicateEmailException(String email) {
        super(HttpStatus.CONFLICT,"이미 사용중인 이메일입니다: "+email);
    }
}
