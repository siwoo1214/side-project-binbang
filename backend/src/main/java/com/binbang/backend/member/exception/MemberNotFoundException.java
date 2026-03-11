package com.binbang.backend.member.exception;

import com.binbang.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends CustomException {

    public MemberNotFoundException(String email) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다: " + email);
    }
}