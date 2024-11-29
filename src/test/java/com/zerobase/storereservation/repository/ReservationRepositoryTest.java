package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.dto.ReservationDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.entity.constants.ReservationStatus;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
import com.zerobase.storereservation.service.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.zerobase.storereservation.exception.ErrorCode.STORE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("Reservation 엔티티 저장 및 조회 테스트")
    void saveAndFindReservation() {
        // given
        User user = User.builder()
                .username("customer")
                .password("password")
                .role("CUSTOMER")
                .build();
        userRepository.save(user);

        Store store = Store.builder()
                .name("store")
                .location("test Street")
                .description("test description")
                .owner(user)
                .build();
        storeRepository.save(store);

        LocalDateTime fixedTime = LocalDateTime.of(2024, 12, 1, 10, 0);
        Reservation reservation = Reservation.builder()
                .store(store)
                .user(user)
                .reservedAt(fixedTime)
                .status(ReservationStatus.CONFIRMED)
                .build();

        // when
        reservationRepository.save(reservation);
        Optional<Reservation> foundReservation = reservationRepository.findById(reservation.getId());

        // then
        assertTrue(foundReservation.isPresent());
        assertEquals(store.getId(), foundReservation.get().getStore().getId());
        assertEquals(user.getId(), foundReservation.get().getUser().getId());
        assertEquals(ReservationStatus.CONFIRMED, foundReservation.get().getStatus());
        assertEquals(fixedTime, foundReservation.get().getReservedAt());
    }
}