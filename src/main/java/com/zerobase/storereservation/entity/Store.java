package com.zerobase.storereservation.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Store
 * 매장 정보를 저장하는 엔티티
 * - 매장 이름, 설명, 소유자 정보, 위치 데이터 등을 관리
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 매장 ID

    @Column(nullable = false)
    private String name; // 매장 이름

    @Column(nullable = false)
    private String description; // 매장 설명

    @ManyToOne(fetch = FetchType.LAZY) // 소유자 정보를 지연 로딩으로 가져옴
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // 매장 소유자 (User와 연관 관계)

    @Column
    private double averageRating; // 매장 평균 별점

    @Transient
    private double distance; // 매장 거리 (계산된 값, DB에 저장되지 않음)

    @Column(nullable = false)
    private Double latitude; // 위도

    @Column(nullable = false)
    private Double longitude; // 경도
}