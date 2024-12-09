package com.zerobase.storereservation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ExceptionController
 * - 글로벌 예외 처리를 담당하는 컨트롤러
 * - CustomException 을 포함한 다양한 예외를 처리
 * - 클라이언트에게 일관된 형식의 JSON 응답을 반환
 */
@RestControllerAdvice
@Slf4j
public class ExceptionController {

    /**
     * CustomException 처리
     * - CustomException 발생 시 로그를 기록하고 적절한 응답을 반환
     *
     * @param c 발생한 CustomException
     * @return ResponseEntity 형태로 클라이언트에게 예외 정보를 반환
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponse> customRequestException(final CustomException c) {
        // 예외 로그 기록
        log.warn("API Exception 발생 - 코드: {}, 메시지: {}",
                c.getErrorCode().getCode(), c.getErrorCode().getDetail());

        ErrorCode errorCode = c.getErrorCode();

        // 클라이언트로 응답 반환
        return ResponseEntity
                .status(errorCode.getHttpStatus()) // HTTP 상태 코드 설정
                .body(new ExceptionResponse(
                        errorCode.getHttpStatus().value(),
                        errorCode.getCode(),
                        errorCode.getDetail()
                ));
    }

    /**
     * ExceptionResponse
     * - 예외 정보를 담아 클라이언트로 전달하는 DTO 클래스
     */
    @Getter
    @AllArgsConstructor
    public static class ExceptionResponse {
        private int status;    // HTTP 상태 코드
        private String code;   // 예외 코드
        private String message; // 예외 메시지
    }
}
