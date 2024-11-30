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
        private int rating;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private Long storeId;
        private Long userId;
        private String content;
        private int rating;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UpdateRequest {
        private String content;
        private int rating;
    }
}
