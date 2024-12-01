package com.zerobase.storereservation.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

public class StoreDto {

    @Data
    public static class CreateRequest {
        private String name;
        private String location;
        private String description;
        private Long ownerId;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String location;
        private String description;
        private Long ownerId;
        private double averageRating;   // 평균 별점 (별점 순 정렬을 위함)
        private double distance;        // 거리순 정렬을 위함
    }
}
