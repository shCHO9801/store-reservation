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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.zerobase.storereservation.entity.constants.Role.PARTNER;
import static com.zerobase.storereservation.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class OwnerReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public List<ReservationDto.Response> getReservationsByStore(
            Long ownerId, Long storeId, LocalDateTime date
    ) {
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

        List<Reservation> reservations =
                reservationRepository.findByStoreIdAndReservedAtBetween(
                        storeId,
                        date.withHour(0).withMinute(0),
                        date.withHour(23).withMinute(59)
                );

        return reservations.stream()
                .map(r -> ReservationDto.Response.builder()
                        .id(r.getId())
                        .storeId(store.getId())
                        .userId(r.getUser().getId())
                        .reservedAt(r.getReservedAt())
                        .status(r.getStatus())
                        .build()
                ).collect(Collectors.toList());
    }

    public List<ReservationDto.Response> getPendingReservations(Long storeId) {
        List<Reservation> reservations =
                reservationRepository.findByStoreIdAndStatus(
                        storeId, ReservationStatus.PENDING
                );
        return reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public ReservationDto.Response approveReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(RESERVATION_NOT_FOUND));

        reservation.setStatus(ReservationStatus.CONFIRMED);
        return convertToDto(reservation);
    }

    @Transactional
    public ReservationDto.Response rejectReservation(
            Long reservationId, ReservationDto.CancelRequest cancelRequest) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(RESERVATION_NOT_FOUND));
        reservation.setStatus(ReservationStatus.REJECTED);
        return convertToDto(reservation);
    }

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
