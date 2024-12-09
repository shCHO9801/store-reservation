package com.zerobase.storereservation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ReviewDto
 * 리뷰 관련 요청 및 응답 데이터를 처리하기 위한 DTO 클래스
 */
public class ReviewDto {

    /**
     * CreateRequest
     * 리뷰 생성 요청 DTO
     * - 사용자가 매장에 대한 리뷰를 작성하기 위한 데이터 구조
     */
    @Data
    public static class CreateRequest {
        private Long storeId;               // 리뷰를 작성할 매장 ID
        private Long userId;                // 리뷰를 작성한 사용자 ID
        private String content;             // 리뷰 내용
        private int rating;                 // 평점 (1 ~ 5)
    }

    /**
     * Response
     * 리뷰 응답 DTO
     * - 작성되거나 조회된 리뷰 정보를 반환하기 위한 데이터 구조
     */
    @Data
    @Builder
    public static class Response {
        private Long id;                    // 리뷰 ID
        private Long storeId;               // 리뷰가 작성된 매장 ID
        private Long userId;                // 리뷰를 작성한 사용자 ID
        private String content;             // 리뷰 내용
        private int rating;                 // 평점 (1 ~ 5)
        private LocalDateTime createdAt;    // 리뷰 작성 시간
    }

    /**
     * UpdateRequest
     * 리뷰 수정 요청 DTO
     * - 사용자가 작성한 리뷰를 수정하기 위한 데이터 구조
     */
    @Data
    public static class UpdateRequest {
        private String content;             // 수정된 리뷰 내용
        private int rating;                 // 수정된 평점 (1 ~ 5)
    }
}
