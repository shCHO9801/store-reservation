package com.zerobase.storereservation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * ErrorCode
 * 애플리케이션에서 발생할 수 있는 모든 에러 코드를 정의
 * - HTTP 상태 코드, 에러 코드, 상세 메시지를 포함
 */
@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // User Errors: 사용자 관련 에러
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER-001", "유저를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER-002", "이미 존재하는 사용자 이름입니다."),
    INCORRECT_PASSWORD(HttpStatus.BAD_REQUEST, "USER-003", "비밀번호가 올바르지 않습니다."),

    // Store Errors: 상점 관련 에러
    STORE_NOT_FOUND(HttpStatus.BAD_REQUEST, "STORE-001", "상점을 찾을 수 없습니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "STORE-002", "상점 소유자가 아니어서 권한이 없습니다."),
    INVALID_CRITERIA(HttpStatus.BAD_REQUEST, "STORE-003", "정렬 조건이 유효하지 않습니다."),
    INVALID_LOCATION(HttpStatus.BAD_REQUEST, "STORE-004", "위치 값이 유효하지 않습니다."),

    // Reservation Errors: 예약 관련 에러
    RESERVATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "RESERVATION-001", "예약을 찾을 수 없습니다."),
    ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "RESERVATION-002", "이미 취소된 예약입니다."),
    INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "RESERVATION-003", "예약 상태가 유효하지 않습니다."),
    INVALID_RESERVATION_TIME(HttpStatus.BAD_REQUEST, "RESERVATION-004", "예약 시간은 현재 시간 이후여야 합니다."),
    ALREADY_REJECTED(HttpStatus.BAD_REQUEST, "RESERVATION-005", "이미 거절된 예약입니다."),
    ALREADY_CONFIRMED(HttpStatus.BAD_REQUEST, "RESERVATION-006", "이미 승인된 예약입니다."),

    // Review Errors: 리뷰 관련 에러
    REVIEW_NOT_FOUND(HttpStatus.BAD_REQUEST, "REVIEW-001", "리뷰가 존재하지 않습니다."),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "REVIEW-002", "평점은 1에서 5 사이의 값이어야 합니다."),
    REVIEW_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "REVIEW-003", "리뷰 내용은 비어 있을 수 없습니다.");

    private final HttpStatus httpStatus; // HTTP 상태 코드
    private final String code;           // 에러 코드 (클라이언트 파싱 용이)
    private final String detail;         // 에러 메시지 (상세 설명)
}
