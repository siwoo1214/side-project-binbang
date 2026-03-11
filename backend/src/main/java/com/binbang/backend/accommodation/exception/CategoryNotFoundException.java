package com.binbang.backend.accommodation.exception;


import com.binbang.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends CustomException {

    public CategoryNotFoundException(Long categoryId) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리 입니다: " + categoryId);
    }
}
