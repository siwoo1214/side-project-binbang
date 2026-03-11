package com.binbang.backend.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  //null인 필드는 JSON 응답에 포함 안 함 (깔끔한 응답)
public class ErrorResponse {

    private final int status;
    private final String message;
    private final LocalDateTime timestamp;
    private final Map<String, String> errors;

}
