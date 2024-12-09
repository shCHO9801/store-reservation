package com.zerobase.storereservation.controller;

import com.zerobase.storereservation.dto.ReviewDto;
import com.zerobase.storereservation.service.ReviewService;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ReviewController
 * 리뷰 관리를 위한 컨트롤러
 * - 리뷰 생성, 조회, 수정, 삭제 기능 제공
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    // 리뷰 관련 비즈니스 로직을 처리하는 서비스
    private final ReviewService reviewService;
    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 리뷰 생성
     * - 새로운 리뷰를 작성합니다.
     *
     * @param request 리뷰 생성 요청 DTO
     * @return 생성된 리뷰 정보
     */
    @PostMapping
    public ResponseEntity<ReviewDto.Response> createReview(
            @RequestBody ReviewDto.CreateRequest request
    ) {
        loggingUtil.logRequest("CREATE REVIEW", request);
        ReviewDto.Response response = reviewService.createReview(request);
        loggingUtil.logSuccess("CREATE REVIEW", "리뷰 ID: " + response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 매장의 리뷰 목록 조회
     * - 매장 ID에 해당하는 리뷰 목록을 반환합니다.
     *
     * @param storeId 매장 ID
     * @return 매장의 리뷰 목록
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ReviewDto.Response>> getReviewsByStore(
            @PathVariable Long storeId
    ) {
        loggingUtil.logRequest("GET REVIEWS BY STORE", storeId);
        List<ReviewDto.Response> reviews = reviewService.getReviewsByStore(storeId);
        loggingUtil.logSuccess("GET REVIEWS BY STORE", "조회된 리뷰 개수: " + reviews.size());
        return ResponseEntity.ok(reviews);
    }

    /**
     * 리뷰 수정
     * - 특정 리뷰를 수정합니다.
     *
     * @param reviewId 리뷰 ID
     * @param userId   사용자 ID (리뷰 작성자 확인용)
     * @param request  리뷰 수정 요청 DTO
     * @return 수정된 리뷰 정보
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto.Response> updateReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId,
            @RequestBody ReviewDto.UpdateRequest request
    ) {
        loggingUtil.logRequest("UPDATE REVIEW", reviewId, userId, request);
        ReviewDto.Response response = reviewService.updateReview(reviewId, userId, request);
        loggingUtil.logSuccess("UPDATE REVIEW", "리뷰 ID: " + response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 삭제
     * - 특정 리뷰를 삭제합니다.
     *
     * @param reviewId 리뷰 ID
     * @param userId   사용자 ID (리뷰 작성자 확인용)
     * @return 성공 여부
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId
    ) {
        loggingUtil.logRequest("DELETE REVIEW", reviewId, userId);
        reviewService.deleteReview(reviewId, userId);
        loggingUtil.logSuccess("DELETE REVIEW", "리뷰 ID: " + reviewId + ", 사용자 ID: " + userId);
        return ResponseEntity.noContent().build();
    }
}
