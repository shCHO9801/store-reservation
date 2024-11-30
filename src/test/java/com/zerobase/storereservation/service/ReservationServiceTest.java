package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.entity.constants.ReservationStatus;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.ReservationRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.zerobase.storereservation.entity.constants.ReservationStatus.CANCELLED;
import static com.zerobase.storereservation.entity.constants.ReservationStatus.CONFIRMED;
import static com.zerobase.storereservation.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("예약 생성 성공")
    void createReservationSuccess() {
        // given
        Store store = Store.builder()
                .id(1L)
                .name("Test Store")
                .build();

        User user = User.builder()
                .id(1L)
                .username("Test User")
                .build();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ReservationDto.CreateRequest request =
                new ReservationDto.CreateRequest();
        request.setStoreId(1L);
        request.setUserId(1L);
        request.setReservedAt(LocalDateTime.of(2024, 12, 1, 10, 30));

        Reservation savedReservation = Reservation.builder()
                .id(1L)
                .store(store)
                .user(user)
                .reservedAt(request.getReservedAt())
                .status(CONFIRMED)
                .build();

        when(reservationRepository.save(any())).thenReturn(savedReservation);

        // when
        ReservationDto.Response response =
                reservationService.createReservation(request);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getStoreId());
        assertEquals(1L, response.getUserId());
        assertEquals(
                LocalDateTime
                        .of(2024, 12, 1, 10, 30),
                response.getReservedAt()
        );
        assertEquals(CONFIRMED, response.getStatus());
    }

    @Test
    @DisplayName("예약 생성 실패 - 상점 없음")
    void createReservationStoreNotFound() {
        // given
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        ReservationDto.CreateRequest request = new ReservationDto.CreateRequest();
        request.setStoreId(1L);
        request.setUserId(1L);
        request.setReservedAt(LocalDateTime.now());

        // then
        CustomException exception = assertThrows(CustomException.class,
                () -> reservationService.createReservation(request));

        assertEquals(STORE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("예약 조회 성공")
    void getReservationByIdSuccess() {
        //given
        Store store = Store.builder()
                .id(1L)
                .name("Test Store")
                .build();

        User user = User.builder()
                .id(1L)
                .username("Test User")
                .build();

        Reservation savedReservation = Reservation.builder()
                .id(1L)
                .store(store)
                .user(user)
                .reservedAt(LocalDateTime.of(2024, 12, 1, 10, 30))
                .status(CONFIRMED)
                .build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(savedReservation));

        //when
        ReservationDto.Response response =
                reservationService.getReservationById(1L);

        //then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getStoreId());
        assertEquals(1L, response.getUserId());
        assertEquals(LocalDateTime.of(2024, 12, 1, 10, 30), response.getReservedAt());
        assertEquals(CONFIRMED, response.getStatus());
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancelReservationSuccess() {
        //given
        Store mockStore = Store.builder()
                .id(1L)
                .name("Test Store")
                .build();

        User mockUser = User.builder()
                .id(1L)
                .username("Test User")
                .build();

        Reservation existingReservation = Reservation.builder()
                .id(1L)
                .store(mockStore)
                .user(mockUser)
                .status(CONFIRMED)
                .reservedAt(LocalDateTime.of(2024, 12, 1, 10, 30))
                .build();

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(existingReservation));

        //when
        ReservationDto.Response response = reservationService.cancelReservation(1L);

        //then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(CANCELLED, response.getStatus());
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("예약 취소 실패 - 예약 없음")
    void cancelReservationStoreNotFound() {
        //given
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> reservationService.cancelReservation(1L));
        assertEquals(RESERVATION_NOT_FOUND, exception.getErrorCode());
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("예약 취소 실패 - 이미 취소된 예약")
    void cancelReservationAlreadyCancelled() {
        //given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .status(CANCELLED)
                .reservedAt(LocalDateTime.of(2024, 12, 1, 10, 30))
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> reservationService.cancelReservation(1L));
        assertEquals(ALREADY_CANCELLED, exception.getErrorCode());
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("도착 확인 성공 - 도착 10분 전")
    void checkArrivalSuccess() {
        //given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservedAt(LocalDateTime.of(2024, 12, 1, 10, 30))
                .status(CONFIRMED)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationDto.CheckArrivalRequest request =
                new ReservationDto.CheckArrivalRequest();
        request.setArrivalTime(LocalDateTime.of(2024, 12, 1, 10, 25));

        //when
        ReservationDto.CheckArrivalResponse response =
                reservationService.checkArrival(1L, request.getArrivalTime());

        //then
        assertNotNull(response);
        assertTrue(response.isArrived());
        assertEquals(1L, response.getReservationId());
    }

    @Test
    @DisplayName("도착 확인 실패 - 예약 없음")
    void checkArrivalReservationNotFound() {
        //given
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> reservationService.checkArrival(1L, LocalDateTime.now()));
        assertEquals(RESERVATION_NOT_FOUND, exception.getErrorCode());
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("도착 확인 실패 - 예약 취소됨")
    void checkArrivalAlreadyCancelled() {
        //given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .status(CANCELLED)
                .reservedAt(LocalDateTime.of(2024, 12, 1, 10, 30))
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> reservationService.checkArrival(1L, LocalDateTime.of(
                        2024, 12, 1, 10, 21
                )));
        assertEquals(ALREADY_CANCELLED, exception.getErrorCode());
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("도착 확인 실패 - 예약 시간 보다 늦게 도착")
    void checkArrivalTooLate() {
        //given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservedAt(LocalDateTime.of(2024, 12, 1, 10, 30))
                .status(CONFIRMED)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        LocalDateTime lateArrival = LocalDateTime.of(2024, 12, 1, 10, 35);

        //when
        ReservationDto.CheckArrivalResponse response =
                reservationService.checkArrival(1L, lateArrival);

        //then
        assertNotNull(response);
        assertFalse(response.isArrived());
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("도착 확인 실패 - 예약 시간보다 10분 이르게 도착")
    void checkArrivalTooEarly() {
        //given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .status(CONFIRMED)
                .reservedAt(LocalDateTime.of(2024, 12, 1, 10, 30))
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        LocalDateTime earlyArrival = LocalDateTime.of(2024, 12, 1, 10, 19);

        //when
        ReservationDto.CheckArrivalResponse response =
                reservationService.checkArrival(1L, earlyArrival);

        //then
        assertNotNull(response);
        assertFalse(response.isArrived());
        verify(reservationRepository, times(1)).findById(1L);
    }
}