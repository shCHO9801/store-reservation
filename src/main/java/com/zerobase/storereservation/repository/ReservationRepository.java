package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
