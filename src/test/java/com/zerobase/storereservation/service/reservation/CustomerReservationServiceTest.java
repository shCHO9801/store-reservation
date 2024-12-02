package com.zerobase.storereservation.service.reservation;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.entity.constants.ReservationStatus;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
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
import java.util.List;
import java.util.Optional;

import static com.zerobase.storereservation.entity.constants.ReservationStatus.CANCELLED;
import static com.zerobase.storereservation.entity.constants.ReservationStatus.CONFIRMED;
import static com.zerobase.storereservation.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CustomerReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private CustomerReservationService reservationService;

    private User user;
    private Store store;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).username("testUser").build();
        store = Store.builder().id(1L).name("testStore").build();
    }

    @Test
    @DisplayName("예약 생성 성공")
    void createReservationSuccess() {
        //given
        ReservationDto.CreateRequest request = new ReservationDto.CreateRequest();
        request.setUserId(user.getId());
        request.setStoreId(store.getId());
        request.setPhoneNumber("010-1234-5678");
        request.setReservedAt(LocalDateTime.now());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

        Reservation savedReservation = Reservation.builder()
                .id(1L)
                .user(user)
                .store(store)
                .phoneNumber("010-1234-5678")
                .reservedAt(request.getReservedAt())
                .build();

        when(reservationRepository.save(any())).thenReturn(savedReservation);

        //when
        ReservationDto.Response result =
                reservationService.createReservation(request);

        //then
        assertNotNull(result);
        assertEquals(savedReservation.getId(), result.getId());
        assertEquals(savedReservation.getUser().getId(), result.getUserId());
        assertEquals(savedReservation.getStore().getId(), result.getStoreId());
        assertEquals(savedReservation.getPhoneNumber(), result.getPhoneNumber());
    }

    @Test
    @DisplayName("예약 생성 실패 - 유저가 존재하지 않음")
    void createReservationFailUserNotFound() {
        //given
        ReservationDto.CreateRequest request = new ReservationDto.CreateRequest();
        request.setUserId(user.getId());
        request.setStoreId(store.getId());
        request.setReservedAt(LocalDateTime.now());

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        //when&then
        CustomException e = assertThrows(CustomException.class,
                () -> reservationService.createReservation(request));
        assertEquals(USER_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("예약 생성 실패 - 매장이 존재하지 않음")
    void createReservationFailStoreNotFound() {
        //given
        ReservationDto.CreateRequest request = new ReservationDto.CreateRequest();
        request.setUserId(user.getId());
        request.setStoreId(store.getId());
        request.setReservedAt(LocalDateTime.now());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(storeRepository.findById(store.getId())).thenReturn(Optional.empty());

        //when&then
        CustomException e = assertThrows(CustomException.class,
                () -> reservationService.createReservation(request));
        assertEquals(STORE_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("예약 조회 성공")
    void getReservationSuccess() {
        //given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(user)
                .store(store)
                .reservedAt(LocalDateTime.now())
                .build();

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        //when
        ReservationDto.Response result =
                reservationService.getReservation(reservation.getId());

        //then
        assertNotNull(result);
        assertEquals(reservation.getId(), result.getId());
        assertEquals(reservation.getUser().getId(), result.getUserId());
        assertEquals(reservation.getStore().getId(), result.getStoreId());
    }

    @Test
    @DisplayName("예약 조회 실패 - 예약이 존재하지 않음")
    void getReservationFailReservationNotFound() {
        //given
        Long invalidId = 99L;
        when(reservationRepository.findById(invalidId)).thenReturn(Optional.empty());

        //when&then
        CustomException e = assertThrows(CustomException.class,
                () -> reservationService.getReservation(invalidId));
        assertEquals(RESERVATION_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancelReservationSuccess() {
        //given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(user)
                .store(store)
                .reservedAt(LocalDateTime.now())
                .status(CONFIRMED)
                .build();

        when(reservationRepository.findById(reservation.getId())).thenReturn(
                Optional.of(reservation));
        //when
        ReservationDto.Response result =
                reservationService.cancelReservation(
                        reservation.getId(), reservation.getUser().getId()
                );

        //then
        assertNotNull(result);
        assertEquals(reservation.getId(), result.getId());
        assertEquals(result.getStatus(), CANCELLED);
    }

    @Test
    @DisplayName("예약 취소 실패 - 권한 없음")
    void cancelReservationFailUnauthorized() {
        //given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(user)
                .store(store)
                .reservedAt(LocalDateTime.now().plusDays(1))
                .status(CONFIRMED)
                .build();

        when(reservationRepository.findById(reservation.getId())).thenReturn(
                Optional.of(reservation));
        //when&then
        CustomException e = assertThrows(CustomException.class,
                () -> reservationService.cancelReservation(
                        reservation.getId(), 99L));
        assertEquals(UNAUTHORIZED_ACTION, e.getErrorCode());
    }

    @Test
    @DisplayName("도착 확인 성공 - 예약자 도착")
    void checkArrivalSuccess() {
        //given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(user)
                .store(store)
                .status(CONFIRMED)
                .reservedAt(LocalDateTime.now().plusMinutes(20))
                .build();

        when(reservationRepository.findById(reservation.getId())).thenReturn(
                Optional.of(reservation)
        );

        //when
        ReservationDto.CheckArrivalResponse result =
                reservationService.checkArrival(
                        reservation.getId(), reservation.getStore().getId(),
                        LocalDateTime.now().plusMinutes(15));

        //then
        assertNotNull(result);
        assertTrue(result.isArrived());
    }

    @Test
    @DisplayName("도착 확인 실패 - 도착 시간 초과")
    void checkArrivalFailLate() {
        //given
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(user)
                .store(store)
                .status(CONFIRMED)
                .reservedAt(LocalDateTime.now())
                .build();

        when(reservationRepository.findById(reservation.getId())).thenReturn(
                Optional.of(reservation)
        );

        //when
        ReservationDto.CheckArrivalResponse result =
                reservationService.checkArrival(
                        reservation.getId(), reservation.getStore().getId(),
                        LocalDateTime.now().plusMinutes(10));

        //then
        assertNotNull(result);
        assertFalse(result.isArrived());
    }

    @Test
    @DisplayName("고객 예약 목록 조회 성공")
    void getCustomerReservationSuccess() {
        //given
        Reservation reservation1 = Reservation.builder()
                .id(1L)
                .user(user)
                .store(store)
                .reservedAt(LocalDateTime.now())
                .status(CONFIRMED)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2L)
                .user(user)
                .store(store)
                .reservedAt(LocalDateTime.now().plusHours(1))
                .status(CONFIRMED)
                .build();

        when(reservationRepository.findByUserId(user.getId()))
                .thenReturn(List.of(reservation1, reservation2));

        //when
        List<ReservationDto.Response> result =
                reservationService.getCustomerReservations(user.getId());

        //then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(reservation1.getId(), result.get(0).getId());
        assertEquals(reservation2.getId(), result.get(1).getId());
    }
}