package com.zerobase.storereservation.service.reservation;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.entity.constants.ReservationStatus;
import com.zerobase.storereservation.entity.constants.Role;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
import com.zerobase.storereservation.repository.ReservationRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import com.zerobase.storereservation.util.LoggingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.zerobase.storereservation.exception.ErrorCode.UNAUTHORIZED_ACTION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class OwnerReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoggingUtil loggingUtil;

    @InjectMocks
    private OwnerReservationService ownerReservationService;

    private User owner;
    private Store store;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        owner = User.builder()
                .id(1L)
                .username("storeOwner")
                .role(Role.PARTNER)
                .build();

        store = Store.builder()
                .id(1L)
                .name("Test Store")
                .owner(owner)
                .build();
    }

    @Test
    @DisplayName("점주 예약 정보 조회 - 성공")
    void getReservationsByStoreSuccess() {
        // given
        LocalDateTime date = LocalDateTime.of(2024, 12, 1, 0, 0);
        Reservation reservation1 = Reservation.builder()
                .id(1L)
                .store(store)
                .user(User.builder().id(2L).username("customer1").build())
                .reservedAt(LocalDateTime.of(2024, 12, 1, 12, 0))
                .build();
        Reservation reservation2 = Reservation.builder()
                .id(2L)
                .store(store)
                .user(User.builder().id(3L).username("customer2").build())
                .reservedAt(LocalDateTime.of(2024, 12, 1, 14, 0))
                .build();

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
        when(reservationRepository.findByStoreIdAndReservedAtBetween(
                store.getId(),
                date.withHour(0).withMinute(0),
                date.withHour(23).withMinute(59)
        )).thenReturn(List.of(reservation1, reservation2));

        // when
        List<ReservationDto.Response> reservations =
                ownerReservationService.getReservationsByStore(owner.getId(), store.getId(), date);

        // then
        assertNotNull(reservations);
        assertEquals(2, reservations.size());
        assertEquals(1L, reservations.get(0).getId());
        assertEquals(2L, reservations.get(1).getId());
    }

    @Test
    @DisplayName("점주 예약 정보 조회 - 실패 (권한 없음)")
    void getReservationsByStoreFailUnauthorizedAction() {
        // given
        owner.setRole(Role.CUSTOMER);
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        // when & then
        CustomException e = assertThrows(CustomException.class,
                () -> ownerReservationService.getReservationsByStore(
                        owner.getId(), store.getId(), LocalDateTime.now())
        );
        assertEquals(UNAUTHORIZED_ACTION, e.getErrorCode());
    }

    @Test
    @DisplayName("점주 예약 정보 조회 - 실패 (매장 없음)")
    void getReservationsByStoreFailStoreNotFound() {
        // given
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.empty());

        // when & then
        CustomException e = assertThrows(CustomException.class,
                () -> ownerReservationService.getReservationsByStore(
                        owner.getId(), store.getId(), LocalDateTime.now()
                )
        );

        assertEquals(ErrorCode.STORE_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("점주 예약 정보 조회 - 실패 (매장 소유자 아님)")
    void getReservationsByStoreFailUnauthorizedStoreOwner() {
        // given
        Store otherStore = Store.builder()
                .id(2L)
                .name("Other Store")
                .owner(User.builder().id(2L).build())
                .build();

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(storeRepository.findById(otherStore.getId())).thenReturn(Optional.of(otherStore));

        // when & then
        CustomException e = assertThrows(CustomException.class,
                () -> ownerReservationService.getReservationsByStore(
                        owner.getId(), otherStore.getId(), LocalDateTime.now()
                ));
        assertEquals(UNAUTHORIZED_ACTION, e.getErrorCode());
    }

    @Test
    @DisplayName("점주가 예약 승인 - 성공")
    void approveReservationSuccess() {
        // given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .store(store)
                .user(User.builder().id(2L).build())
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findById(reservation.getId()))
                .thenReturn(Optional.of(reservation));

        // when
        ReservationDto.Response result =
                ownerReservationService.approveReservation(reservation.getId());

        // then
        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
    }

    @Test
    @DisplayName("점주가 예약 거절 - 성공")
    void rejectReservationSuccess() {
        // given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .store(store)
                .user(User.builder().id(2L).build())
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findById(reservation.getId()))
                .thenReturn(Optional.of(reservation));

        // when
        ReservationDto.Response result =
                ownerReservationService.rejectReservation(
                        reservation.getId(),
                        new ReservationDto.CancelRequest("점주가 거절함")
                );

        // then
        assertEquals(ReservationStatus.REJECTED, result.getStatus());
    }
}