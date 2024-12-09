package com.zerobase.storereservation.service.reservation;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
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

import static com.zerobase.storereservation.entity.constants.ReservationStatus.CANCELLED;
import static com.zerobase.storereservation.entity.constants.ReservationStatus.CONFIRMED;
import static com.zerobase.storereservation.exception.ErrorCode.*;

/**
 * CustomerReservationService
 * - 고객 예약 관리 비즈니스 로직을 처리하는 서비스
 * - 예약 생성, 조회, 취소, 도착 확인, 고객 예약 목록 조회 기능 제공
 */
@Service
@RequiredArgsConstructor
public class CustomerReservationService {

    // 예약 관련 데이터 베이스 작업을 처리하는 Repository
    private final ReservationRepository reservationRepository;

    // 매장 관련 데이터 베이스 작업을 처리하는 Repository
    private final StoreRepository storeRepository;

    // 사용자 관련 데이터 베이스 작업을 처리하는 Repository
    private final UserRepository userRepository;

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 예약 생성
     * - 사용자와 매장을 검증한 후 예약을 생성
     *
     * @param request 예약 생성 요청 DTO
     * @return 생성된 예약 정보
     */
    public ReservationDto.Response createReservation(
            ReservationDto.CreateRequest request
    ) {
        loggingUtil.logRequest("CREATE RESERVATION", request);

        User user = findUserById(request.getUserId());
        Store store = findStoreById(request.getStoreId());

        // 예약 생성 및 검증
        Reservation reservation = Reservation.builder()
                .user(user)
                .store(store)
                .phoneNumber(request.getPhoneNumber())
                .reservedAt(request.getReservedAt())
                .status(CONFIRMED)
                .build();

        reservation.validateReservationTime();  // 예약 시간이 유효한지 확인

        ReservationDto.Response response = saveAndConvertToResponse(reservation);

        loggingUtil.logSuccess("CREATE RESERVATION", response);
        return response;
    }

    /**
     * 특정 예약 조회
     * - 예약 ID 를 사용하여 예약 정보를 조회
     *
     * @param id 예약 ID
     * @return 예약 정보 DTO
     */
    public ReservationDto.Response getReservation(Long id) {
        loggingUtil.logRequest("GET RESERVATION", id);

        Reservation reservation = findReservationById(id);
        ReservationDto.Response response = convertToResponse(reservation);

        loggingUtil.logSuccess("GET RESERVATION", response);
        return response;
    }

    /**
     * 예약 취소
     * - 예약 상태를 취소로 변경
     *
     * @param ReservationId 예약 ID
     * @param userId        사용자 ID
     * @return 취소된 예약 정보
     */
    @Transactional
    public ReservationDto.Response cancelReservation(Long ReservationId, Long userId) {
        loggingUtil.logRequest("CANCEL RESERVATION", ReservationId, userId);

        Reservation reservation = findReservationById(ReservationId);

        validateUserAuthorization(reservation.getUser().getId(), userId);
        if (reservation.getStatus() == CANCELLED) {
            throw new CustomException(ALREADY_CANCELLED);
        }

        reservation.setStatus(CANCELLED);
        ReservationDto.Response response = convertToResponse(reservation);

        loggingUtil.logSuccess("CANCEL RESERVATION", response);
        return response;
    }

    /**
     * 예약 도착 확인
     * - 도착 시간이 예약 시간 범위 내에 있는지 확인
     *
     * @param reservationId 예약 ID
     * @param storeId       매장 ID
     * @param arrivalTime   도착 시간
     * @return 도착 확인 결과
     */
    @Transactional
    public ReservationDto.CheckArrivalResponse checkArrival(
            Long reservationId, Long storeId, LocalDateTime arrivalTime) {
        loggingUtil.logRequest("CHECK ARRIVAL", reservationId, storeId, arrivalTime);

        Reservation reservation = findReservationById(reservationId);

        validateUserAuthorization(reservation.getUser().getId(), storeId);
        if (reservation.getStatus() == CANCELLED) {
            throw new CustomException(ALREADY_CANCELLED);
        }

        boolean arrived = arrivalTime.isAfter(
                reservation.getReservedAt().minusMinutes(10))
                && arrivalTime.isBefore(reservation.getReservedAt());

        ReservationDto.CheckArrivalResponse response = ReservationDto.CheckArrivalResponse.builder()
                .reservationId(reservation.getId())
                .arrived(arrived)
                .build();

        loggingUtil.logSuccess("CHECK ARRIVAL", response);
        return response;
    }

    /**
     * 특정 사용자의 예약 목록 조회
     * - 사용자가 생성한 모든 예약을 반환
     *
     * @param userId 사용자 ID
     * @return 예약 목록 DTO
     */
    public List<ReservationDto.Response> getCustomerReservations(Long userId) {
        loggingUtil.logRequest("GET CUSTOMER RESERVATIONS", userId);

        List<Reservation> reservations = reservationRepository.findByUserId(userId);

        List<ReservationDto.Response> responses = reservations.stream()
                .map(this::convertToResponse)
                .toList();

        loggingUtil.logSuccess("GET CUSTOMER RESERVATIONS", "예약 개수: " + responses.size());
        return responses;
    }

    // ==== Private Helper Methods ====

    /**
     * 사용자 ID로 사용자 조회
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    /**
     * 매장 ID로 매장 조회
     */
    private Store findStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
    }

    /**
     * 예약 ID로 예약 조회
     */
    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(RESERVATION_NOT_FOUND));
    }

    /**
     * 사용자 ID와 요청자 ID의 일치 여부 확인
     */
    private void validateUserAuthorization(Long reservationId, Long requestUserId) {
        if (!reservationId.equals(requestUserId)) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }
    }

    /**
     * 예약을 저장하고 Response DTO 로 변환
     */
    private ReservationDto.Response saveAndConvertToResponse(Reservation reservation) {
        reservation = reservationRepository.save(reservation);
        return convertToResponse(reservation);
    }

    /**
     * 예약 객체를 Response DTO 로 변환
     */
    private ReservationDto.Response convertToResponse(Reservation reservation) {
        return ReservationDto.Response.builder()
                .id(reservation.getId())
                .storeId(reservation.getStore().getId())
                .userId(reservation.getUser().getId())
                .phoneNumber(reservation.getPhoneNumber())
                .reservedAt(reservation.getReservedAt())
                .status(reservation.getStatus())
                .build();
    }
}
