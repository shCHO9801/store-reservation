package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
