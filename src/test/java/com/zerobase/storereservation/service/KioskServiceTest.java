package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.ReservationRepository;
import com.zerobase.storereservation.util.LoggingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class KioskServiceTest {

    @InjectMocks
    private KioskService kioskService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private LoggingUtil loggingUtil;

    private Store mockStore;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockStore = Store.builder()
                .id(1L)
                .name("Test Store")
                .build();

        mockUser = User.builder()
                .id(1L)
                .username("Test User")
                .build();
    }

    @Test
    @DisplayName("특정 매장의 당일 예약 목록 조회 성공")
    void getTodayReservationsSuccess() {
        // given
        Reservation reservation1 = Reservation.builder()
                .id(1L)
                .store(mockStore)
                .user(mockUser)
                .reservedAt(LocalDateTime.now().withHour(12).withMinute(0))
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2L)
                .store(mockStore)
                .user(mockUser)
                .reservedAt(LocalDateTime.now().withHour(14).withMinute(0))
                .build();

        when(reservationRepository.findByStoreIdAndReservedAtBetween(
                eq(mockStore.getId()),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(Arrays.asList(reservation1, reservation2));

        // when
        List<ReservationDto.Response> responses = kioskService.getTodayReservations(mockStore.getId());

        // then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(2L, responses.get(1).getId());

        verify(reservationRepository, times(1)).findByStoreIdAndReservedAtBetween(
                eq(mockStore.getId()),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("도착 확인 성공")
    void checkArrivalSuccess() {
        // given
        Long reservationId = 1L;
        LocalDateTime reservedAt = LocalDateTime.now().withHour(12).withMinute(20);
        LocalDateTime arrivalTime = LocalDateTime.now().withHour(12).withMinute(15); // 유효 범위 내 도착 시간

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .store(mockStore)
                .user(mockUser)
                .reservedAt(reservedAt)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when
        ReservationDto.CheckArrivalResponse response = kioskService.checkArrival(reservationId, arrivalTime);

        // then
        assertNotNull(response, "Response should not be null");
        assertTrue(response.isArrived(), "Arrival check should return true");
        verify(reservationRepository, times(1)).findById(reservationId);
    }


    @Test
    @DisplayName("도착 확인 실패 - 예약 ID를 찾을 수 없음")
    void checkArrivalFailReservationNotFound() {
        // given
        Long invalidReservationId = 99L;
        LocalDateTime arrivalTime = LocalDateTime.now();

        when(reservationRepository.findById(invalidReservationId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> kioskService.checkArrival(invalidReservationId, arrivalTime));
        assertEquals("RESERVATION_NOT_FOUND", exception.getErrorCode().name());
        verify(reservationRepository, times(1)).findById(invalidReservationId);
    }

    @Test
    @DisplayName("도착 확인 실패 - 도착 시간이 유효하지 않음")
    void checkArrivalFailInvalidTime() {
        // given
        Long reservationId = 1L;
        LocalDateTime arrivalTime = LocalDateTime.now().withHour(12).withMinute(30); // 유효 시간 이후
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .store(mockStore)
                .user(mockUser)
                .reservedAt(LocalDateTime.now().withHour(12).withMinute(20))
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when
        ReservationDto.CheckArrivalResponse response = kioskService.checkArrival(reservationId, arrivalTime);

        // then
        assertNotNull(response);
        assertFalse(response.isArrived());
        verify(reservationRepository, times(1)).findById(reservationId);
    }
}
