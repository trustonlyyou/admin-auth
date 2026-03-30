package com.levely.auth.exception;

import lombok.AccessLevel;
import lombok.Builder;

/**
 * API 에러 응답 본문을 표준화한다.
 * <pre>
 * 처리 규칙:
 * 	- HTTP status, 응답 코드, 사용자 메시지를 함께 내려준다.
 * 	- 기본 메시지는 ErrorCode를 따르되, 검증 실패처럼 구체 메시지가 있으면 덮어쓴다.
 * </pre>
 */
@Builder(access = AccessLevel.PRIVATE)
public record ErrorResponse(
        int status,
        String code,
        String message
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .message(message)
                .build();
    }
}
