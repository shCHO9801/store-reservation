package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.ReservationRepository;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.zerobase.storereservation.exception.ErrorCode.RESERVATION_NOT_FOUND;

/**
 * KioskService
 * 키오스크 관련 비즈니스 로직을 처리
 * - 도착 확인 및 예약 목록 조회 기능 제공
 */
@Service
@RequiredArgsConstructor
public class KioskService {
    // 예약 관련 데이터 작업을 처리하는 Repository
    private final ReservationRepository reservationRepository;

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 특정 매장의 당일 예약 목록 조회
     * - 매장 ID 기준으로 예약 정보를 반환
     *
     * @param storeId 매장 ID
     * @return List<ReservationDto.Response> 당일 예약 리스트
     */
    public List<ReservationDto.Response> getTodayReservations(Long storeId) {
        loggingUtil.logRequest("GET TODAY RESERVATIONS", storeId);

        List<Reservation> reservations = reservationRepository.findByStoreIdAndReservedAtBetween(
                storeId,
                LocalDateTime.now().withHour(0).withMinute(0),
                LocalDateTime.now().withHour(23).withMinute(59)
        );

        List<ReservationDto.Response> responses = convertToResponseList(reservations);
        loggingUtil.logSuccess("GET TODAY RESERVATIONS", "예약 개수: " + responses.size());

        return responses;
    }

    /**
     * 도착 확인
     * - 예약 시간에 기반하여 도착 여부를 판단
     *
     * @param reservationId 예약 ID
     * @param arrivalTime   도착 시간
     * @return ReservationDto.CheckArrivalResponse 도착 확인 결과 DTO
     */
    public ReservationDto.CheckArrivalResponse checkArrival(Long reservationId, LocalDateTime arrivalTime) {
        loggingUtil.logRequest("CHECK ARRIVAL", reservationId, arrivalTime);

        Reservation reservation = findReservationById(reservationId);
        boolean arrived = arrivalTime.isAfter(reservation.getReservedAt().minusMinutes(10)) &&
                arrivalTime.isBefore(reservation.getReservedAt());

        ReservationDto.CheckArrivalResponse response = ReservationDto.CheckArrivalResponse.builder()
                .reservationId(reservation.getId())
                .arrived(arrived)
                .build();

        loggingUtil.logSuccess("CHECK ARRIVAL", response);
        return response;
    }

    // ==== Private Helper Methods ====

    /**
     * 예약 ID로 예약 조회
     *
     * @param reservationId 예약 ID
     * @return Reservation 예약 엔티티
     */
    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    loggingUtil.logError("FIND RESERVATION", "예약 ID를 찾을 수 없음: " + reservationId);
                    return new CustomException(RESERVATION_NOT_FOUND);
                });
    }

    /**
     * 예약 엔티티 리스트를 Response DTO 리스트로 변환
     *
     * @param reservations 예약 엔티티 리스트
     * @return List<ReservationDto.Response> 예약 응답 DTO 리스트
     */
    private List<ReservationDto.Response> convertToResponseList(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> ReservationDto.Response.builder()
                        .id(reservation.getId())
                        .storeId(reservation.getStore().getId())
                        .userId(reservation.getUser().getId())
                        .phoneNumber(reservation.getPhoneNumber())
                        .reservedAt(reservation.getReservedAt())
                        .status(reservation.getStatus())
                        .build())
                .toList();
    }
}
