package com.zerobase.storereservation.dto;

import lombok.Builder;
import lombok.Data;

public class StoreDto {

    @Data
    public static class CreateRequest {
        private String name;
        private String description;
        private Long ownerId;
        private Double latitude;
        private Double longitude;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private Long ownerId;
        private double averageRating;   // 평균 별점 (별점 순 정렬을 위함)
        private double distance;        // 거리순 정렬을 위함
        private Double latitude;
        private Double longitude;
    }
}
