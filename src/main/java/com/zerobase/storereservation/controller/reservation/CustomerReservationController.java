package com.zerobase.storereservation.controller.reservation;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.service.reservation.CustomerReservationService;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CustomerReservationController
 * 고객의 예약 관리를 위한 컨트롤러
 * - 예약 생성, 조회, 도착 확인 등의 기능 제공
 */
@RestController
@RequestMapping("/api/customer/reservations")
@RequiredArgsConstructor
public class CustomerReservationController {

    // 고객 예약 관련 비즈니스 로직을 처리하는 서비스
    private final CustomerReservationService reservationService;

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 예약 생성
     * - 고객이 새로운 예약을 생성
     *
     * @param request 예약 생성 요청 DTO
     * @return 생성된 예약 정보
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ReservationDto.Response> createReservation(
            @RequestBody ReservationDto.CreateRequest request
    ) {
        loggingUtil.logRequest("CREATE RESERVATION", request);
        ReservationDto.Response response = reservationService.createReservation(request);
        loggingUtil.logSuccess("CREATE RESERVATION", response);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 예약 조회
     * - 예약 ID 를 통해 예약 정보를 조회
     *
     * @param id 예약 ID
     * @return 예약 정보
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto.Response> getReservation(
            @PathVariable Long id
    ) {
        loggingUtil.logRequest("GET RESERVATION", id);
        ReservationDto.Response response = reservationService.getReservation(id);
        loggingUtil.logSuccess("GET RESERVATION", response);
        return ResponseEntity.ok(response);
    }

    /**
     * 예약 도착 확인
     * - 예약 ID, 매장 ID 및 도착 시간을 확인하여 예약 도착 상태를 업데이트
     *
     * @param id          예약 ID
     * @param storeId     매장 ID
     * @param arrivalTime 도착 시간
     * @return 도착 확인 결과
     */
    @PostMapping("/{id}/check-arrival")
    public ResponseEntity<ReservationDto.CheckArrivalResponse> checkArrival(
            @PathVariable Long id,
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime arrivalTime
    ) {
        loggingUtil.logRequest(
                "CHECK ARRIVAL",
                "예약 ID : " + id
                        + ", 매장 ID : " + storeId
                        + ", 도착 시간" + arrivalTime);
        ReservationDto.CheckArrivalResponse response =
                reservationService.checkArrival(id, storeId, arrivalTime);
        loggingUtil.logSuccess("CHECK ARRIVAL", response);
        return ResponseEntity.ok(response);
    }

    /**
     * 고객의 모든 예약 조회
     * - 특정 고객의 모든 예약 리스트를 반환
     *
     * @param userId 고객 ID
     * @return 고객의 예약 리스트
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ReservationDto.Response>> getReservations(
            @RequestParam Long userId
    ) {
        loggingUtil.logRequest("GET RESERVATIONS", userId);
        List<ReservationDto.Response> reservations =
                reservationService.getCustomerReservations(userId);
        loggingUtil.logSuccess("GET RESERVATIONS",
                "예약 개수 : " + reservations);
        return ResponseEntity.ok(reservations);
    }
}