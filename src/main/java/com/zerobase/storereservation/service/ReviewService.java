package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.ReviewDto;
import com.zerobase.storereservation.entity.Review;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
import com.zerobase.storereservation.repository.ReviewRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.zerobase.storereservation.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final StoreService storeService;

    public ReviewDto.Response createReview(ReviewDto.CreateRequest request) {
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new CustomException(ErrorCode.INVALID_RATING);
        }

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Review review = Review.builder()
                .store(store)
                .user(user)
                .content(request.getContent())
                .rating(request.getRating())
                .createdAt(LocalDateTime.now())
                .build();

        review = reviewRepository.save(review);

        storeService.updateAverageRating(store.getId());

        return ReviewDto.Response.builder()
                .id(review.getId())
                .storeId(store.getId())
                .userId(user.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto.Response> getReviewsByStore(Long storeId) {
        List<Review> reviews = reviewRepository.findByStoreId(storeId);
        return reviews.stream()
                .map(review -> ReviewDto.Response.builder()
                        .id(review.getId())
                        .storeId(review.getStore().getId())
                        .userId(review.getUser().getId())
                        .rating(review.getRating())
                        .content(review.getContent())
                        .createdAt(review.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDto.Response updateReview(Long reviewId, Long userId, ReviewDto.UpdateRequest request) {
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new CustomException(ErrorCode.INVALID_RATING);
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }

        review.setContent(request.getContent());
        review.setRating(request.getRating());

        storeService.updateAverageRating(review.getStore().getId());

        return ReviewDto.Response.builder()
                .id(review.getId())
                .storeId(review.getStore().getId())
                .userId(review.getUser().getId())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .build();
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND));

        Long storeOwnerId = review.getStore().getOwner().getId();
        Long reviewerId = review.getUser().getId();

        if (!storeOwnerId.equals(userId) && !reviewerId.equals(userId)) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }
        reviewRepository.deleteById(reviewId);

        storeService.updateAverageRating(review.getStore().getId());
    }
}
