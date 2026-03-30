package com.levely.auth.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ErrorCode 정책에 맞춰 예외 응답을 공통 처리한다.
 * 처리 규칙:
 * 	- 비즈니스 예외는 선언된 ErrorCode를 그대로 응답에 반영한다.
 * 	- 입력 검증 실패는 INVALID_INPUT_VALUE로 묶되, 첫 번째 검증 메시지를 우선 노출한다.
 * 	- 처리되지 않은 예외는 INTERNAL_SERVER_ERROR로 변환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.warn("Business exception occurred. code={}, message={}", errorCode.getCode(), errorCode.getMessage(), exception);
        return buildResponse(errorCode);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationException(Exception exception) {
        String message = resolveValidationMessage(exception);

        log.warn("Validation exception occurred. message={}", message, exception);
        return buildResponse(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException exception) {
        log.warn("Method not allowed. method={}", exception.getMethod(), exception);
        return buildResponse(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Unhandled exception occurred.", exception);
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode) {
        return buildResponse(errorCode, errorCode.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, message));
    }

    /**
     * 검증 예외에서 사용자에게 바로 보여줄 메시지를 선택한다.
     * 처리 규칙:
     * 	- 필드 검증 메시지가 있으면 첫 번째 메시지를 사용한다.
     * 	- ConstraintViolation 기반 검증은 첫 번째 violation 메시지를 사용한다.
     * 	- 세부 메시지가 없으면 공통 INVALID_INPUT_VALUE 메시지로 폴백한다.
     */
    private String resolveValidationMessage(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            return resolveFieldErrorMessage(methodArgumentNotValidException.getBindingResult().getFieldError());
        }

        if (exception instanceof BindException bindException) {
            return resolveFieldErrorMessage(bindException.getBindingResult().getFieldError());
        }

        if (exception instanceof ConstraintViolationException constraintViolationException) {
            return constraintViolationException.getConstraintViolations()
                    .stream()
                    .findFirst()
                    .map(ConstraintViolation::getMessage)
                    .orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage());
        }

        return ErrorCode.INVALID_INPUT_VALUE.getMessage();
    }

    private String resolveFieldErrorMessage(FieldError fieldError) {
        if (fieldError == null || fieldError.getDefaultMessage() == null) {
            return ErrorCode.INVALID_INPUT_VALUE.getMessage();
        }

        return fieldError.getDefaultMessage();
    }
}
