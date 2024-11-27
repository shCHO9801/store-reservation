package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
    @DisplayName("Reservation 엔티티 저장 및 조회 서비스")
    void savaAndFindReservation() {
        //given
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

        Reservation reservation = Reservation.builder()
                .store(store)
                .user(user)
                .reserved_at(LocalDateTime.now())
                .status("CONFIRMED")
                .build();

        //when
        reservationRepository.save(reservation);
        Optional<Reservation> foundReservation = reservationRepository.findById(reservation.getId());

        //then
        assertTrue(foundReservation.isPresent());
        assertEquals(reservation.getStore().getId(), foundReservation.get().getStore().getId());
        assertEquals(reservation.getUser().getId(), foundReservation.get().getUser().getId());
        assertEquals(reservation.getStatus(), foundReservation.get().getStatus());
        assertEquals(reservation.getReserved_at(), foundReservation.get().getReserved_at());
    }
}