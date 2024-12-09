package com.zerobase.storereservation.dto;

import lombok.Builder;
import lombok.Data;

/**
 * StoreDto
 * 매장 관련 요청 및 응답 데이터를 처리하기 위한 DTO 클래스
 */
public class StoreDto {

    /**
     * CreateRequest
     * 매장 생성 요청 DTO
     * - 매장을 생성하기 위한 입력 데이터를 전달하는 클래스
     */
    @Data
    public static class CreateRequest {
        private String name;            // 매장 이름
        private String description;     // 매장 설명
        private Long ownerId;           // 매장 소유자 (점주) ID
        private Double latitude;        // 매장 위도
        private Double longitude;       // 매장 경도
    }

    /**
     * Response
     * 매장 응답 DTO
     * - 매장 정보를 반환하기 위한 데이터 구조
     */
    @Data
    @Builder
    public static class Response {
        private Long id;                // 매장 ID
        private String name;            // 매장 이름
        private String description;     // 매장 설명
        private Long ownerId;           // 매장 소유자 ID
        private double averageRating;   // 평균 별점 (별점 순 정렬을 위함)
        private double distance;        // 거리순 정렬을 위함
        private Double latitude;        // 매장 위도
        private Double longitude;       // 매장 경도
    }
}
