package com.zerobase.storereservation.controller;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationDto.Response> createReservation(
            @RequestBody ReservationDto.CreateRequest request
    ) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto.Response> getReservationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }
}
