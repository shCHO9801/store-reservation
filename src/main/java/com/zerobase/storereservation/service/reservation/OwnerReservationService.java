package com.zerobase.storereservation.service.reservation;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.entity.constants.ReservationStatus;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.ReservationRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.zerobase.storereservation.entity.constants.ReservationStatus.CONFIRMED;
import static com.zerobase.storereservation.entity.constants.ReservationStatus.REJECTED;
import static com.zerobase.storereservation.entity.constants.Role.PARTNER;
import static com.zerobase.storereservation.exception.ErrorCode.*;

/**
 * OwnerReservationService
 * - 점주가 자신의 매장 예약을 관리할 수 있도록 지원하는 서비스
 * - 예약 목록 조회, 승인, 거절 등 기능 제공
 */
@Service
@RequiredArgsConstructor
public class OwnerReservationService {

    // 예약 관련 데이터 베이스 작업을 처리하는 Repository
    private final ReservationRepository reservationRepository;

    // 사용자 관련 데이터 베이스 작업을 처리하는 Repository
    private final UserRepository userRepository;

    // 매장 관련 데이터 베이스 작업을 처리하는 Repository
    private final StoreRepository storeRepository;

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 특정 매장의 날짜별 예약 목록 조회
     * - 점주가 자신의 매장에서 예약된 정보를 조회
     *
     * @param ownerId 점주 ID
     * @param storeId 매장 ID
     * @param date    조회할 날짜
     * @return 예약 목록 DTO
     */
    public List<ReservationDto.Response> getReservationsByStore(
            Long ownerId, Long storeId, LocalDateTime date
    ) {
        loggingUtil.logRequest("GET RESERVATIONS BY STORE");

        validateStoreOwner(ownerId, storeId);

        List<Reservation> reservations =
                reservationRepository.findByStoreIdAndReservedAtBetween(
                        storeId,
                        date.withHour(0).withMinute(0),
                        date.withHour(23).withMinute(59)
                );

        List<ReservationDto.Response> responses = reservations.stream()
                .map(this::convertToDto)
                .toList();

        loggingUtil.logSuccess("GET RESERVATIONS BY STORE", "예약 개수: " + responses.size());
        return responses;
    }

    /**
     * 특정 매장의 대기 중인 예약 목록 조회
     *
     * @param storeId 매장 ID
     * @return 대기 중인 예약 목록 DTO
     */
    public List<ReservationDto.Response> getPendingReservations(Long storeId) {
        loggingUtil.logRequest("GET PENDING RESERVATIONS", storeId);

        List<Reservation> reservations =
                reservationRepository.findByStoreIdAndStatus(
                        storeId, ReservationStatus.PENDING
                );

        List<ReservationDto.Response> responses = reservations.stream()
                .map(this::convertToDto)
                .toList();

        loggingUtil.logSuccess("GET PENDING RESERVATIONS", "대기 중 예약 개수: " + responses.size());
        return responses;
    }

    /**
     * 예약 승인
     * - 특정 예약의 상태를 승인으로 병경
     *
     * @param reservationId 예약 ID
     * @return 승인된 예약 정보 DTO
     */
    @Transactional
    public ReservationDto.Response approveReservation(Long reservationId) {
        loggingUtil.logRequest("APPROVE RESERVATION", reservationId);

        Reservation reservation = findReservationById(reservationId);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new CustomException(INVALID_RESERVATION_STATUS);
        }

        reservation.setStatus(CONFIRMED);
        ReservationDto.Response response = convertToDto(reservation);

        loggingUtil.logSuccess("APPROVE RESERVATION", response);
        return response;
    }

    /**
     * 예약 거절
     * - 특정 예약의 상태를 거절로 변경
     *
     * @param reservationId 예약 ID
     * @param cancelRequest 거절 사유
     * @return 거절된 예약 정보 DTO
     */
    @Transactional
    public ReservationDto.Response rejectReservation(
            Long reservationId, ReservationDto.CancelRequest cancelRequest) {
        loggingUtil.logRequest("REJECT RESERVATION", reservationId, cancelRequest);

        Reservation reservation = findReservationById(reservationId);

        if (reservation.getStatus() == REJECTED) {
            throw new CustomException(ALREADY_REJECTED);
        }

        if (reservation.getStatus() == CONFIRMED) {
            throw new CustomException(ALREADY_CONFIRMED);
        }
        reservation.setStatus(REJECTED);

        ReservationDto.Response response = convertToDto(reservation);

        loggingUtil.logSuccess("REJECT RESERVATION", response);
        return response;
    }

    // ==== Private Helper Methods ====

    /**
     * 점주와 매장 ID를 검증하고 매장 정보를 반환
     * - 점주가 해당 매장을 소유하고 있는지 확인
     *
     * @param ownerId 점주의 사용자 ID
     * @param storeId 매장의 ID
     * @throws CustomException 점주나 매장 정보를 찾을 수 없거나 권한이 없을 경우
     */
    private void validateStoreOwner(Long ownerId, Long storeId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (!owner.getRole().equals(PARTNER)) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        if (!store.getOwner().getId().equals(ownerId)) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }
    }

    /**
     * 예약 ID로 예약 정보를 조회
     * - 예약 정보를 데이터베이스에서 검색
     *
     * @param reservationId 예약의 ID
     * @return 조회된 예약 엔티티
     * @throws CustomException 예약 정보를 찾을 수 없는 경우
     */
    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(RESERVATION_NOT_FOUND));
    }

    /**
     * 예약 엔티티를 Response DTO 로 변환
     * - 클라이언트에 반환하기 위해 엔티티를 DTO 형태로 변환
     *
     * @param reservation 변환할 예약 엔티티
     * @return 변환된 예약 Response DTO
     */
    private ReservationDto.Response convertToDto(Reservation reservation) {
        return ReservationDto.Response.builder()
                .id(reservation.getId())
                .storeId(reservation.getStore().getId())
                .userId(reservation.getUser().getId())
                .reservedAt(reservation.getReservedAt())
                .status(reservation.getStatus())
                .build();
    }
}