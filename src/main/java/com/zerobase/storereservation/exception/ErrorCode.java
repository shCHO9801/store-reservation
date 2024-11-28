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
    // Reservation Error
    RESERVATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "RESERVATION-001", "예약을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;      // 에러 코드를 숫자로 변환하면 클라이언트에서 파싱하기 편함
    private final String detail;
}
