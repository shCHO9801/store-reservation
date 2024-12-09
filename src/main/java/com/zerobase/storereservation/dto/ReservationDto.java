package com.zerobase.storereservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zerobase.storereservation.entity.constants.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ReservationDto
 * 예약 관련 데이터 전송 객체
 * - 요청 및 응답을 위한 DTO 정의
 */
public class ReservationDto {

    /**
     * CreateRequest
     * 예약 생성 요청 DTO
     */
    @Data
    public static class CreateRequest {
        private Long storeId;               // 예약 대상 매장 ID
        private Long userId;                // 예약자 ID
        private String phoneNumber;         // 연락처
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime reservedAt;   // 예약 시간
    }

    /**
     * Response
     * 예약 응답 DTO
     */
    @Data
    @Builder
    public static class Response {
        private Long id;                    // 예약 ID
        private Long storeId;               // 매장 ID
        private Long userId;                // 예약자 ID
        private String phoneNumber;         // 연락처
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime reservedAt;   // 예약 시간
        private ReservationStatus status;   // 예약 상태
    }

    /**
     * CancelRequest
     * 예약 취소 요청 DTO
     */
    @Data
    public static class CancelRequest {
        private String reason;              // 취소 사유

        // 생성자 제공
        public CancelRequest(String reason) {
            this.reason = reason;
        }
    }

    /**
     * CheckArrivalResponse
     * 예약 도착 확인 응답 DTO
     */
    @Data
    @Builder
    public static class CheckArrivalResponse {
        private Long reservationId;         // 예약 ID
        private boolean arrived;            // 도착 여부
    }
}
