package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByStoreId(Long storeId);
}
