package com.zerobase.storereservation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    // User Error
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER-001", "유저를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER-002", "이미 존재하는 사용자 이름입니다."),

    // Store Error
    STORE_NOT_FOUND(HttpStatus.BAD_REQUEST, "STORE-001", "상점을 찾을 수 없습니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "STORE-002", "상점 소유자가 아니어서 권한이 없습니다."),
    INVALID_CRITERIA(HttpStatus.BAD_REQUEST, "STORE-003", "정렬 조건이 유효하지 않습니다."),
    INVALID_LOCATION(HttpStatus.BAD_REQUEST, "STORE-004", "위치 값이 유효하지 않습니다."),

    // Reservation Error
    RESERVATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "RESERVATION-001", "예약을 찾을 수 없습니다."),
    ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "RESERVATION-002", "이미 취소된 예약입니다."),

    //Review Error
    REVIEW_NOT_FOUND(HttpStatus.BAD_REQUEST, "REVIEW-001", "리뷰가 존재하지 않습니다."),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "REVIEW-002", "평점은 1에서 5 사이의 값이어야 합니다.");

    private final HttpStatus httpStatus;
    private final String code;      // 에러 코드를 숫자로 변환하면 클라이언트에서 파싱하기 편함
    private final String detail;
}
