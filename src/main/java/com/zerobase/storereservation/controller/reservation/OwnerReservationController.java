package com.zerobase.storereservation.controller.reservation;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.service.reservation.OwnerReservationService;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OwnerReservationController
 * 점주가 예약 정보를 관리하기 위한 컨트롤러
 * - 예약 조회, 승인, 거절 등의 기능 제공
 */
@RestController
@RequestMapping("/api/owner/reservations")
@RequiredArgsConstructor
public class OwnerReservationController {

    // 예약 관련 비즈니스 로직을 처리하는 서비스
    private final OwnerReservationService reservationService;
    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 매장의 특정 날짜 예약 목록 조회
     * - 점주가 자신의 매장에서 예약된 정보를 조회
     *
     * @param storeId 매장 ID
     * @param ownerId 점주 ID
     * @param date    조회할 날짜
     * @return 해당 매장의 예약 목록
     */
    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/store/{storeId}/owner/{ownerId}")
    public ResponseEntity<List<ReservationDto.Response>> getReservationsByStore(
            @PathVariable Long storeId,
            @PathVariable Long ownerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        loggingUtil.logRequest("GET RESERVATION BY STORE", storeId, ownerId, date);
        List<ReservationDto.Response> reservations =
                reservationService.getReservationsByStore(storeId, ownerId, date);
        loggingUtil.logSuccess("GET RESERVATION BY STORE",
                "조회된 예약 개수: " + reservations.size());
        return ResponseEntity.ok(reservations);
    }

    /**
     * 매장의 대기 중인 예약 목록 조회
     * - 점주가 자신의 매장에서 승인 대기 상태인 예약 목록을 조회
     *
     * @param storeId 매장 ID
     * @return 대기 중인 예약 목록
     */
    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/pending")
    public ResponseEntity<List<ReservationDto.Response>> getPendingReservations(
            @RequestParam Long storeId
    ) {
        loggingUtil.logRequest("GET PENDING RESERVATIONS", storeId);
        List<ReservationDto.Response> reservations =
                reservationService.getPendingReservations(storeId);
        loggingUtil.logSuccess(
                "GET PENDING RESERVATIONS",
                "대기 중인 예약 개수 : " + reservations.size());
        return ResponseEntity.ok(reservations);
    }

    /**
     * 예약 승인
     * - 점주가 특정 예약을 승인
     *
     * @param reservationId 승인할 예약 ID
     * @return 승인된 예약 정보
     */
    @PreAuthorize("hasRole('PARTNER')")
    @PutMapping("/{reservationId}/approve")
    public ResponseEntity<ReservationDto.Response> approveReservation(
            @PathVariable Long reservationId
    ) {
        loggingUtil.logRequest("PUT APPROVE RESERVATION", reservationId);
        ReservationDto.Response response =
                reservationService.approveReservation(reservationId);
        loggingUtil.logSuccess(
                "PUT APPROVE RESERVATION",
                "예약 ID : " + response.getId() +
                        ", 상태 : " + response.getStatus()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 예약 거절
     * - 점주가 특정 예약을 거절하며, 거절 사유를 전달
     *
     * @param reservationId 거절할 예약 ID
     * @param cancelRequest 거절 사유를 포함한 요청 DTO
     * @return 거절된 예약 정보
     */
    @PreAuthorize("hasRole('PARTNER')")
    @PutMapping("/{reservationId}/reject")
    public ResponseEntity<ReservationDto.Response> rejectReservation(
            @PathVariable Long reservationId,
            @RequestBody ReservationDto.CancelRequest cancelRequest
    ) {
        loggingUtil.logRequest("REJECT RESERVATION",
                "예약 ID : " + reservationId +
                        ", 거절 사유 : " + cancelRequest.getReason());
        ReservationDto.Response response =
                reservationService.rejectReservation(reservationId, cancelRequest);
        loggingUtil.logSuccess("REJECT RESERVATION", response);
        return ResponseEntity.ok(response);
    }
}