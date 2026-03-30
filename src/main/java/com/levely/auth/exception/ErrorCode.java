package com.levely.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "E400", "잘못된 요청입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "E405", "허용되지 않은 HTTP 메서드입니다."),

    // 비즈니스 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U404", "사용자를 찾을 수 없습니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "U409", "이미 존재하는 사용자입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
