package com.zerobase.storereservation.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * LoggingUtil
 * 공통 로깅 유틸리티 클래스
 * - 일관된 로깅 포맷 제공
 */
@Component
@Slf4j
public class LoggingUtil {
    /**
     * 요청 로그 기록
     *
     * @param action 동작 이름
     * @param args   요청 정보
     */
    public void logRequest(String action, Object... args) {
        log.info("[{}] 요청 - {}", action, args);
    }

    /**
     * 성공 로그 기록
     *
     * @param action 동작 이름
     * @param result 응답 정보
     */
    public void logSuccess(String action, Object result) {
        log.info("[{}] 성공 - {}", action, result);
    }

    /**
     * 에러 로그 기록
     *
     * @param action 동작 이름
     * @param error  에러 메시지
     */
    public void logError(String action, String error) {
        log.error("[{}] 에러 - {}", action, error);
    }
}
