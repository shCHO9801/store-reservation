package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.ReviewDto;
import com.zerobase.storereservation.entity.Review;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.ReservationRepository;
import com.zerobase.storereservation.repository.ReviewRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.zerobase.storereservation.entity.constants.ReservationStatus.CONFIRMED;
import static com.zerobase.storereservation.exception.ErrorCode.*;

/**
 * ReviewService
 * - 매장 리뷰 관리 비즈니스 로직을 처리
 * - 리뷰 생성, 조회, 수정, 삭제 기능 제공
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    // 리뷰 관련 데이터 작업을 처리하는 Repository
    private final ReviewRepository reviewRepository;

    // 사용자 관련 데이터 작업을 처리하는 Repository
    private final UserRepository userRepository;

    // 매점 관련 데이터 작업을 처리하는 Repository
    private final StoreRepository storeRepository;

    // 예약 관련 데이터 작업을 처리하는 Repository
    private final ReservationRepository reservationRepository;

    // 매장 관련 비즈니스 로직을 처리하는 서비스
    private final StoreService storeService;

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 리뷰 생성
     * - 유효한 예약 상태를 확인한 뒤 리뷰를 생성
     *
     * @param request 리뷰 생성 요청 DTO
     * @return 생성된 리뷰 정보 DTO
     */
    public ReviewDto.Response createReview(ReviewDto.CreateRequest request) {
        loggingUtil.logRequest("CREATE REVIEW", request);

        validateRating(request.getRating());
        validateReservationExists(request.getUserId(), request.getStoreId());

        Store store = findStoreById(request.getStoreId());
        User user = findUserById(request.getUserId());

        Review review = Review.builder()
                .store(store)
                .user(user)
                .content(request.getContent())
                .rating(request.getRating())
                .createdAt(LocalDateTime.now())
                .build();

        review.validateReviewData();
        review = reviewRepository.save(review);

        storeService.updateAverageRating(store.getId());
        ReviewDto.Response response = convertToDto(review);

        loggingUtil.logSuccess("CREATE REVIEW", response);
        return response;
    }

    /**
     * 특정 매장의 리뷰 목록 조회
     *
     * @param storeId 매장 ID
     * @return 매장 리뷰 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<ReviewDto.Response> getReviewsByStore(Long storeId) {
        loggingUtil.logRequest("GET REVIEWS BY STORE", storeId);

        List<Review> reviews = reviewRepository.findByStoreId(storeId);
        List<ReviewDto.Response> responses = reviews.stream()
                .map(this::convertToDto)
                .toList();

        loggingUtil.logSuccess("GET REVIEWS BY STORE", "리뷰 개수: " + responses.size());
        return responses;
    }

    /**
     * 리뷰 수정
     *
     * @param reviewId 리뷰 ID
     * @param userId   사용자 ID
     * @param request  리뷰 수정 요청 DTO
     * @return 수정된 리뷰 정보 DTO
     */
    @Transactional
    public ReviewDto.Response updateReview(Long reviewId, Long userId, ReviewDto.UpdateRequest request) {
        loggingUtil.logRequest("UPDATE REVIEW", reviewId, userId, request);

        validateRating(request.getRating());
        Review review = findReviewById(reviewId);

        validateUserAuthorization(review.getUser().getId(), userId);

        review.setContent(request.getContent());
        review.setRating(request.getRating());

        storeService.updateAverageRating(review.getStore().getId());
        ReviewDto.Response response = convertToDto(review);

        loggingUtil.logSuccess("UPDATE REVIEW", response);
        return response;
    }

    /**
     * 리뷰 삭제
     *
     * @param reviewId 리뷰 ID
     * @param userId   사용자 ID
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        loggingUtil.logRequest("DELETE REVIEW", reviewId, userId);

        Review review = findReviewById(reviewId);

        validateDeleteAuthorization(review, userId);

        reviewRepository.deleteById(reviewId);
        storeService.updateAverageRating(review.getStore().getId());

        loggingUtil.logSuccess("DELETE REVIEW", "리뷰 ID: " + reviewId);
    }

    // ==== Private Helper Methods ====

    /**
     * 평점 유효성 검증
     *
     * @param rating 평점 값
     * @throws CustomException 평점이 1 ~ 5 범위를 벗어난 경우
     */
    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new CustomException(INVALID_RATING);
        }
    }

    /**
     * 예약 존재 여부 확인
     *
     * @param storeId 사용자 ID
     * @param userId  매장 ID
     * @throws CustomException 예약이 없을 경우
     */
    private void validateReservationExists(Long userId, Long storeId) {
        boolean isReservationValid =
                reservationRepository.existsByUserIdAndStoreIdAndStatus(
                        userId, storeId, CONFIRMED
                );
        if (!isReservationValid) {
            throw new CustomException(RESERVATION_NOT_FOUND);
        }
    }

    /**
     * 사용자 ID로 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 조회된 사용자 엔티티
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    /**
     * 매장 ID로 매장 조회
     *
     * @param storeId 매장 ID
     * @return 조회된 매장 엔티티
     */
    private Store findStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
    }

    /**
     * 리뷰 ID로 리뷰 조회
     *
     * @param reviewId 리뷰 ID
     * @return 조회된 리뷰 엔티티
     */
    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND));
    }

    /**
     * 사용자 권한 확인
     *
     * @param reviewerId 리뷰 작성자 ID
     * @param userId     요청자 ID
     * @throws CustomException 요청자가 작성자가 아닌 경우
     */
    private void validateUserAuthorization(Long reviewerId, Long userId) {
        if (!reviewerId.equals(userId)) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }
    }

    /**
     * 삭제 권한 확인
     *
     * @param review 리뷰 엔티티
     * @param userId 요청자 ID
     * @throws CustomException 요청자가 권한이 없는 경우
     */
    private void validateDeleteAuthorization(Review review, Long userId) {
        Long storeOwnerId = review.getStore().getOwner().getId();
        Long reviewerId = review.getUser().getId();
        if (!storeOwnerId.equals(userId) && !reviewerId.equals(userId)) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }
    }

    /**
     * 리뷰 엔티티를 Response DTO 로 변환
     *
     * @param review 리뷰 엔티티
     * @return 변환된 리뷰 Response DTO
     */
    private ReviewDto.Response convertToDto(Review review) {
        return ReviewDto.Response.builder()
                .id(review.getId())
                .storeId(review.getStore().getId())
                .userId(review.getUser().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
