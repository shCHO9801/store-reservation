package com.zerobase.storereservation.entity.constants;

/**
 * ReservationStatus
 * 예약 상태를 나타내는 열거형
 * - 예약의 상태를 구분하기 위한 상수 정의
 */
public enum ReservationStatus {
    PENDING,        // 예약 요청 상태
    CONFIRMED,      // 예약 승인 상태
    CANCELLED,      // 예약 취소 상태
    REJECTED        // 예약 거절된 상태
}
