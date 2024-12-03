package com.zerobase.storereservation.controller.reservation;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.service.reservation.OwnerReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/owner/reservations")
@RequiredArgsConstructor
public class OwnerReservationController {

    private final OwnerReservationService reservationService;

    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/store/{storeId}/owner/{ownerId}")
    public ResponseEntity<List<ReservationDto.Response>> getReservationsByStore(
            @PathVariable Long storeId,
            @PathVariable Long ownerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
            ){
        return ResponseEntity.ok(reservationService.getReservationsByStore(ownerId,storeId,date));
    }
}
