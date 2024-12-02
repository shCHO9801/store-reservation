package com.zerobase.storereservation.service.reservation;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.ReservationRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
