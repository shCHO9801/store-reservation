package com.zerobase.storereservation.controller.reservation;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.service.reservation.CustomerReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/reservations")
@RequiredArgsConstructor
public class CustomerReservationController {

    private final CustomerReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationDto.Response> createReservation(
            @RequestBody ReservationDto.CreateRequest request
    ) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto.Response> getReservation(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(reservationService.getReservation(id));
    }
}
