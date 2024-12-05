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

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("select r " +
            "from Reservation r " +
            "where r.store.id = :storeId and r.reservedAt " +
            "between :startDate and :endDate")
    List<Reservation> findByStoreIdAndReservedAtBetween(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("select case when count(r) > 0 then true else false end " +
            "from Reservation r " +
            "where r.user.id = :userId and r.store.id = :storeId " +
            "and r.status = :status")
    boolean existsByUserIdAndStoreIdAndStatus(
            @Param("userId") Long userId,
            @Param("storeId") Long storeId,
            @Param("status") ReservationStatus reservationStatus);

    @Query("select r " +
            "from Reservation r " +
            "where r.user.id = :userId " +
            "order by r.reservedAt desc ")
    List<Reservation> findByUserId(@Param("userId") Long userId);

    List<Reservation> findByStoreIdAndStatus(
            Long storeId, ReservationStatus reservationStatus);
}
