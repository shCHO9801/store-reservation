package com.zerobase.storereservation.exception;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponse> customRequestException(final CustomException c) {
        log.warn("API Exception : {}", c.getErrorCode());
        ErrorCode errorCode = c.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ExceptionResponse(
                        errorCode.getHttpStatus().value(),
                        errorCode.getCode(),
                        errorCode.getDetail()
                ));
    }

    @Getter
    @AllArgsConstructor
    public static class ExceptionResponse {
        private int status;
        private String code;
        private String message;
    }
}
