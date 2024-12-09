package com.zerobase.storereservation.entity;

import com.zerobase.storereservation.exception.CustomException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.zerobase.storereservation.exception.ErrorCode.INVALID_RATING;
import static com.zerobase.storereservation.exception.ErrorCode.REVIEW_CONTENT_EMPTY;

/**
 * Review
 * 매장 리뷰 정보를 저장하는 엔티티
 * - 특정 매장(Store)에 대해 작성된 사용자(User)의 리뷰 데이터를 관리
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 리뷰 ID

    @ManyToOne(fetch = FetchType.LAZY) // 매장 정보를 지연 로딩으로 가져옴
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // 리뷰가 작성된 매장

    @ManyToOne(fetch = FetchType.LAZY) // 사용자 정보를 지연 로딩으로 가져옴
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 리뷰 작성자

    @Column(nullable = false, length = 1000) // 최대 길이 제한 추가
    private String content; // 리뷰 내용

    @Column(nullable = false)
    private int rating; // 평점 (1~5 범위)

    @Column(nullable = false, updatable = false) // 작성 시간은 수정 불가
    private LocalDateTime createdAt; // 리뷰 작성 시간

    /**
     * 리뷰 데이터 검증 로직
     * - 평점은 1에서 5 사이여야 함
     * - 내용은 비어 있을 수 없음
     */
    public void validateReviewData() {
        if (rating < 1 || rating > 5) {
            throw new CustomException(INVALID_RATING);

        }
        if (content == null || content.trim().isEmpty()) {
            throw new CustomException(REVIEW_CONTENT_EMPTY);
        }
    }

    /**
     * 리뷰 생성 시점 기록
     * - 작성 시간을 자동으로 설정
     */
    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * toString 메서드 오버라이드
     * - 주요 데이터만 출력하여 디버깅에 활용
     */
    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", storeId=" + (store != null ? store.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                ", rating=" + rating +
                ", createdAt=" + createdAt +
                '}';
    }
}
