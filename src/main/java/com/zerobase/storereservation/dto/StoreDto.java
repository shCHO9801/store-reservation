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
    }
}
