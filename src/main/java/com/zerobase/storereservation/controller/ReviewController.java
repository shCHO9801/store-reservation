package com.zerobase.storereservation.controller;

import com.zerobase.storereservation.dto.ReviewDto;
import com.zerobase.storereservation.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto.Response> createReview(
            @RequestBody ReviewDto.CreateRequest request
    ) {
        return ResponseEntity.ok(reviewService.createReview(request));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ReviewDto.Response>> getReviewsByStore(
            @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByStore(storeId));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto.Response> updateReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId,
            @RequestBody ReviewDto.UpdateRequest request
    ) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, request));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ReviewDto.Response> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId
    ) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
}
