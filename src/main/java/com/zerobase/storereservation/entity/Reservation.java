package com.zerobase.storereservation.entity;

import com.zerobase.storereservation.entity.constants.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 예약 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // 예약된 매장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 예약한 사용자

    @Column(nullable = false)
    private String phoneNumber;     // 예약자 전화번호

    @Column(nullable = false)
    private LocalDateTime reservedAt; // 예약 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status; // 예약 상태 (CONFIRMED, CANCELLED 등)
}