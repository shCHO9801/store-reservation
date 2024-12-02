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
                .reservedAt(request.getReservedAt())
                .status(CONFIRMED)
                .build();

        reservation = reservationRepository.save(reservation);
        return ReservationDto.Response.builder()
                .id(reservation.getId())
                .storeId(store.getId())
                .userId(user.getId())
                .reservedAt(reservation.getReservedAt())
                .status(reservation.getStatus())
                .build();
    }

    public ReservationDto.Response getReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
        return ReservationDto.Response.builder()
                .id(reservation.getId())
                .storeId(reservation.getStore().getId())
                .userId(reservation.getUser().getId())
                .reservedAt(reservation.getReservedAt())
                .status(reservation.getStatus())
                .build();
    }

}
