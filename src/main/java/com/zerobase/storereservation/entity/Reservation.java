package com.zerobase.storereservation.entity;

import com.zerobase.storereservation.entity.constants.ReservationStatus;
import com.zerobase.storereservation.exception.CustomException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.zerobase.storereservation.exception.ErrorCode.INVALID_RESERVATION_TIME;

/**
 * Reservation
 * 예약 정보를 나타내는 엔티티
 * - 사용자와 매장 간의 예약 정보를 저장
 */
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

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩으로 관련 매장을 필요할 때만 로드
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // 예약된 매장

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩으로 관련 사용자를 필요할 때만 로드
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 예약한 사용자

    @Column(nullable = false)
    private String phoneNumber; // 예약자 전화번호

    @Column(nullable = false)
    private LocalDateTime reservedAt; // 예약 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status; // 예약 상태 (기본값: PENDING)

    @PrePersist
    private void prePersist() {
        if (status == null) {
            status = ReservationStatus.PENDING;
        }
    }

    public void validateReservationTime() {
        if (reservedAt.isBefore(LocalDateTime.now())) {
            throw new CustomException(INVALID_RESERVATION_TIME);
        }
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", storeId=" + (store != null ? store.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                ", reservedAt=" + reservedAt +
                ", status=" + status +
                '}';
    }
}
