package com.zerobase.storereservation.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

/**
 * JwtException
 * - JWT 인증 과정에서 발생하는 예외를 처리하기 위한 커스텀 예외 클래스
 * - JWT 오류 타입을 명시적으로 관리
 */
@Getter
public class JwtException extends AuthenticationException {

    // JWT 관련 에러 타입
    private final JwtErrorType errorType;

    /**
     * JwtException 생성자
     * - 특정 JwtErrorType 을 기반으로 예외 생성
     *
     * @param errorType 발생한 JWT 에러 타입
     */
    public JwtException(JwtErrorType errorType) {
        super(errorType.getMessage()); // 부모 클래스의 메시지 설정
        this.errorType = errorType;
    }

    /**
     * JwtErrorType
     * - JWT 인증 오류에 대한 타입 정의
     */
    public enum JwtErrorType {
        EXPIRED_TOKEN("토큰이 만료되었습니다."),       // 만료된 JWT 토큰
        INVALID_TOKEN("유효하지 않은 토큰입니다.");   // 유효하지 않은 JWT 토큰

        // 에러 메시지
        private final String message;

        /**
         * JwtErrorType 생성자
         * - 각 에러 타입에 대응하는 메시지를 설정
         *
         * @param message 에러 메시지
         */
        JwtErrorType(String message) {
            this.message = message;
        }

        /**
         * 에러 메시지 반환
         *
         * @return 에러 메시지
         */
        public String getMessage() {
            return message;
        }
    }
}