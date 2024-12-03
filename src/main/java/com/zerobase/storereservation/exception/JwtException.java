package com.zerobase.storereservation.exception;

import org.springframework.security.core.AuthenticationException;
import lombok.Getter;

@Getter
public class JwtException extends AuthenticationException {
    private final JwtErrorType errorType;

    public JwtException(JwtErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public enum JwtErrorType {
        EXPIRED_TOKEN("토큰이 만료되었습니다."),
        INVALID_TOKEN("유효하지 않은 토큰입니다.");

        private final String message;

        JwtErrorType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
