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
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.zerobase.storereservation.entity.constants.ReservationStatus.CONFIRMED;
import static com.zerobase.storereservation.exception.ErrorCode.STORE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private ReservationService reservationService;

    public ReservationServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
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
}