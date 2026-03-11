package com.binbang.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex){
        log.error(ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .status(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(response);
    }

    // 1. Validation 에러 처리 (@Valid 검증 실패 시)
    @ExceptionHandler(MethodArgumentNotValidException.class) //MethodArgumentNotValidException 발생 시 자동으로 실행
    public ResponseEntity<ErrorResponse> handelEValidationException(MethodArgumentNotValidException ex){

        // 에러 로그 출력
        log.error("@Validation 에러 발생: {}",ex.getMessage());

        // 필드 별 에러 메세지를 담을 Map 생성
        Map<String, String> errors = new HashMap<>();

        // 모든 필드 에러를 순회하면서 Map에 추가
        ex.getBindingResult().getAllErrors().forEach(error->{
            String fieldName = ((FieldError)error).getField(); // 에러 발생한 필드명
            String errorMessage = error.getDefaultMessage(); // 에러 메세지
            errors.put(fieldName,errorMessage);
        });

        // ErrorResponse 객체 생성
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())  //400만 따로 갖고오기
                .message("입력값 검증에 실패했습니다 (@Valid)")
                .timestamp(LocalDateTime.now())
                .errors(errors)  // 필드 별 모든 에러 상세
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    // 2. 비즈니스 로직 에러 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {

        log.error("Business logic error occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    // 3. 예상치 못한 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {

        log.error("Unexpected error occurred: ", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

}
