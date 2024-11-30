package com.zerobase.storereservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;                // 리뷰가 작성된 매장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                  // 리뷰가 작성자

    @Column(nullable = false)
    private String content;             //리뷰 내용

    @Column(nullable = false)
    private int rating;                 // 평점

    @Column(nullable = false)
    private LocalDateTime createdAt;    // 리뷰 작성 시간
}
