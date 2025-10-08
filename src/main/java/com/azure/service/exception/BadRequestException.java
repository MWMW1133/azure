package com.azure.service.exception;

/**
 * 잘못된 입력 값 또는 허용되지 않는 상태 전이를 시도할 때 던지는 예외.
 * 1차 검증은 컨트롤러/Validator에서 수행하고, 핵심 규칙은 서비스에서 한 번 더 방어한다.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
