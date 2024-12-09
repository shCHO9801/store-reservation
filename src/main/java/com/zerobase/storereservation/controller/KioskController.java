package com.zerobase.storereservation.controller;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.service.KioskService;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * KioskController
 * 키오스크 기능을 위한 컨트롤러
 * - 예약 목록 조회 및 도착 확인 기능 제공
 */
@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class KioskController {

    // 키오스크 관련 비즈니스 로직을 처리하는 서비스
    private final KioskService kioskService;

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 특정 매장의 당일 예약 목록 조회
     *
     * @param storeId 매장 ID
     * @return ResponseEntity<List < ReservationDto.Response>> 당일 예약 리스트 응답
     */
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDto.Response>> getTodayReservations(
            @RequestParam Long storeId
    ) {
        loggingUtil.logRequest("GET TODAY RESERVATIONS", storeId);
        List<ReservationDto.Response> reservations = kioskService.getTodayReservations(storeId);
        loggingUtil.logSuccess("GET TODAY RESERVATIONS", "예약 개수: " + reservations.size());
        return ResponseEntity.ok(reservations);
    }

    /**
     * 도착 확인
     * - 예약 시간에 기반하여 도착 여부를 확인
     *
     * @param id          예약 ID
     * @param arrivalTime 도착 시간
     * @return ResponseEntity<ReservationDto.CheckArrivalResponse> 도착 확인 결과 응답
     */
    @PostMapping("/reservations/{id}/check-arrival")
    public ResponseEntity<ReservationDto.CheckArrivalResponse> checkArrival(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrivalTime
    ) {
        loggingUtil.logRequest("CHECK ARRIVAL", "예약 ID: " + id + ", 도착 시간: " + arrivalTime);
        ReservationDto.CheckArrivalResponse response = kioskService.checkArrival(id, arrivalTime);
        loggingUtil.logSuccess("CHECK ARRIVAL", response);
        return ResponseEntity.ok(response);
    }
}
