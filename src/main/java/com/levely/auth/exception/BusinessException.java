package com.levely.auth.exception;

import lombok.Getter;

/**
 * ErrorCode 기반으로 비즈니스 실패를 전달한다.
 * 처리 규칙:
 * 	- 서비스 계층은 정책 위반 상황을 ErrorCode 하나로 표현한다.
 * 	- GlobalExceptionHandler는 이 예외를 받아 HTTP 상태와 응답 본문을 일관되게 변환한다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
