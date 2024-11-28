package com.zerobase.storereservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zerobase.storereservation.entity.constants.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class ReservationDto {

    @Data
    public static class CreateRequest {
        private Long storeId;
        private Long userId;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime reservedAt;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private Long storeId;
        private Long userId;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime reservedAt;

        private ReservationStatus status;
    }
}
