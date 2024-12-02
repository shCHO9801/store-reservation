package com.zerobase.storereservation.controller.reservation;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.service.reservation.CustomerReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customer/reservations")
@RequiredArgsConstructor
public class CustomerReservationController {

    private final CustomerReservationService reservationService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ReservationDto.Response> createReservation(
            @RequestBody ReservationDto.CreateRequest request
    ) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto.Response> getReservation(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(reservationService.getReservation(id));
    }

    @PostMapping("/{id}/check-arrival")
    public ResponseEntity<ReservationDto.CheckArrivalResponse> checkArrival(
            @PathVariable Long id,
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime arrivalTime
    ) {
        return ResponseEntity.ok(
                reservationService.checkArrival(id, storeId, arrivalTime));
    }

    @GetMapping
    public ResponseEntity<List<ReservationDto.Response>> getReservations(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(reservationService.getCustomerReservations(userId));
    }
}
