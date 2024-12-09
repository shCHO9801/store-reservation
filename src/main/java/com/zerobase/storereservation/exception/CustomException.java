package com.zerobase.storereservation.exception;

import lombok.Getter;

/**
 * CustomException
 * - 비즈니스 로직에서 발생하는 사용자 정의 예외
 * - ErrorCode 를 기반으로 예외 세부 정보를 관리
 */
@Getter
public class CustomException extends RuntimeException {

    // 예외 세부 정보를 나타내는 ErrorCode
    private final ErrorCode errorCode;

    /**
     * CustomException 생성자
     * - ErrorCode 를 기반으로 예외를 생성
     *
     * @param errorCode 예외의 원인을 나타내는 ErrorCode 객체
     */
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getDetail());
        this.errorCode = errorCode;

        // 예외 발생 시 로그 출력
        logException(errorCode);
    }

    // ==== Private Helper Methods ====

    /**
     * 예외 발생 시 로그 출력
     *
     * @param errorCode 발생한 예외의 ErrorCode
     */
    private void logException(ErrorCode errorCode) {
        // 여기에서는 콘솔 출력으로 대체, 로깅 유틸 사용 가능
        System.err.println("CustomException 발생 - 코드: "
                + errorCode.getCode() + ", 메시지: " + errorCode.getDetail());
    }
}
