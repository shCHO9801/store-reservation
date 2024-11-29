package com.zerobase.storereservation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class ReviewDto {

    @Data
    public static class CreateRequest {
        private Long storeId;
        private Long userId;
        private String content;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private Long storeId;
        private Long userId;
        private String content;
        private LocalDateTime createdAt;
    }
}
