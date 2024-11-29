package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.ReviewDto;
import com.zerobase.storereservation.entity.Review;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.ReviewRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public ReviewDto.Response createReview(ReviewDto.CreateRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Review review = Review.builder()
                .store(store)
                .user(user)
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        review = reviewRepository.save(review);

        return ReviewDto.Response.builder()
                .id(review.getId())
                .storeId(store.getId())
                .userId(user.getId())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public List<ReviewDto.Response> getReviewsByStore(Long storeId) {
        List<Review> reviews = reviewRepository.findByStoreId(storeId);
        return reviews.stream()
                .map(review -> ReviewDto.Response.builder()
                        .id(review.getId())
                        .storeId(review.getStore().getId())
                        .userId(review.getUser().getId())
                        .content(review.getContent())
                        .createdAt(review.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new CustomException(REVIEW_NOT_FOUND);
        }
        reviewRepository.deleteById(reviewId);
    }
}
