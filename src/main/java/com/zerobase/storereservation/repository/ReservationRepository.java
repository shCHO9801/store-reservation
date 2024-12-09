package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.constants.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ReservationRepository
 * - 예약 관련 데이터를 처리하기 위한 JPA Repository
 * - 사용자 및 매장 예약 데이터에 대한 커스텀 쿼리를 정의
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 특정 매장의 지정된 시간 범위 내 예약 조회
     *
     * @param storeId 매장 ID
     * @param startDate 검색 시작 시간
     * @param endDate 검색 종료 시간
     * @return 예약 리스트
     */
    @Query("select r " +
            "from Reservation r " +
            "where r.store.id = :storeId and r.reservedAt " +
            "between :startDate and :endDate")
    List<Reservation> findByStoreIdAndReservedAtBetween(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 사용자와 매장의 예약 여부 확인
     * - 상태에 따라 예약 존재 여부 반환
     *
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     * @param reservationStatus 예약 상태
     * @return 예약 존재 여부 (true/false)
     */
    @Query("select case when count(r) > 0 then true else false end " +
            "from Reservation r " +
            "where r.user.id = :userId and r.store.id = :storeId " +
            "and r.status = :status")
    boolean existsByUserIdAndStoreIdAndStatus(
            @Param("userId") Long userId,
            @Param("storeId") Long storeId,
            @Param("status") ReservationStatus reservationStatus);

    /**
     * 특정 사용자 ID로 예약 리스트 조회
     * - 예약 시간 기준 내림차순 정렬
     *
     * @param userId 사용자 ID
     * @return 예약 리스트
     */
    @Query("select r " +
            "from Reservation r " +
            "where r.user.id = :userId " +
            "order by r.reservedAt desc ")
    List<Reservation> findByUserId(@Param("userId") Long userId);

    /**
     * 특정 매장과 예약 상태에 따른 예약 조회
     *
     * @param storeId 매장 ID
     * @param reservationStatus 예약 상태
     * @return 예약 리스트
     */
    List<Reservation> findByStoreIdAndStatus(
            Long storeId, ReservationStatus reservationStatus);
}
