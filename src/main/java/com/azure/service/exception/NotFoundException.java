package com.azure.service.exception;

/**
 * 요청한 엔티티가 존재하지 않을 때 던지는 예외.
 * 서비스 레이어에서는 Optional을 그대로 반환하지 말고, 비어 있을 경우 이 예외로 변환한다.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
