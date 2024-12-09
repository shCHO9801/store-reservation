package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ReviewRepository
 * - 리뷰 데이터를 처리하기 위한 JPA Repository
 * - 매장 리뷰에 대한 기본 및 커스텀 쿼리를 정의
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * 특정 매장의 모든 리뷰 조회
     * - 매장 ID 를 기준으로 리뷰 데이터를 검색
     *
     * @param storeId 매장 ID
     * @return 매장의 리뷰 리스트
     */
    List<Review> findByStoreId(Long storeId);
}
