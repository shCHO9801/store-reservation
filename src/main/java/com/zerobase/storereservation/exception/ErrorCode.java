package com.zerobase.storereservation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "U001", "존재하지 않는 유저 입니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자 이름입니다.");

    private final HttpStatus httpStatus;
    private final String code;      // 에러 코드를 숫자로 변환하면 클라이언트에서 파싱하기 편함
    private final String detail;
}
