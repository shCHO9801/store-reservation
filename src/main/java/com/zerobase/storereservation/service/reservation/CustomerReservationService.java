package com.zerobase.storereservation.service.reservation;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
import com.zerobase.storereservation.repository.ReservationRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.zerobase.storereservation.entity.constants.ReservationStatus.CANCELLED;
import static com.zerobase.storereservation.entity.constants.ReservationStatus.CONFIRMED;
import static com.zerobase.storereservation.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CustomerReservationService {
    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public ReservationDto.Response createReservation(
            ReservationDto.CreateRequest request
    ) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        Reservation reservation = Reservation.builder()
                .user(user)
                .store(store)
                .phoneNumber(request.getPhoneNumber())
                .reservedAt(request.getReservedAt())
                .status(CONFIRMED)
                .build();

        reservation = reservationRepository.save(reservation);
        return ReservationDto.Response.builder()
                .id(reservation.getId())
                .storeId(store.getId())
                .userId(user.getId())
                .phoneNumber(request.getPhoneNumber())
                .reservedAt(reservation.getReservedAt())
                .status(reservation.getStatus())
                .build();
    }

    public ReservationDto.Response getReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new CustomException(RESERVATION_NOT_FOUND));
        return ReservationDto.Response.builder()
                .id(reservation.getId())
                .storeId(reservation.getStore().getId())
                .userId(reservation.getUser().getId())
                .reservedAt(reservation.getReservedAt())
                .status(reservation.getStatus())
                .build();
    }
    @Transactional
    public ReservationDto.Response cancelReservation(Long ReservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(ReservationId)
                .orElseThrow(() -> new CustomException(RESERVATION_NOT_FOUND));
        if(!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }

        if (reservation.getStatus() == CANCELLED) {
            throw new CustomException(ALREADY_CANCELLED);
        }

        reservation.setStatus(CANCELLED);

        return ReservationDto.Response.builder()
                .id(reservation.getId())
                .storeId(reservation.getStore().getId())
                .userId(reservation.getUser().getId())
                .reservedAt(reservation.getReservedAt())
                .status(reservation.getStatus())
                .build();
    }

    @Transactional
    public ReservationDto.CheckArrivalResponse checkArrival(
            Long reservationId, Long storeId, LocalDateTime arrivalTime) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(RESERVATION_NOT_FOUND));

        if(!reservation.getStore().getId().equals(storeId)) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }

        if(reservation.getStatus() == CANCELLED){
            throw new CustomException(ALREADY_CANCELLED);
        }

        boolean arrived = arrivalTime.isAfter(
                reservation.getReservedAt().minusMinutes(10))
                && arrivalTime.isBefore(reservation.getReservedAt());

        return ReservationDto.CheckArrivalResponse.builder()
                .reservationId(reservation.getId())
                .arrived(arrived)
                .build();
    }

    public List<ReservationDto.Response> getCustomerReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream()
                .map(reservation -> ReservationDto.Response.builder()
                        .id(reservation.getId())
                        .storeId(reservation.getStore().getId())
                        .userId(reservation.getUser().getId())
                        .phoneNumber(reservation.getPhoneNumber())
                        .reservedAt(reservation.getReservedAt())
                        .status(reservation.getStatus())
                        .build()).collect(Collectors.toList());
    }
}
