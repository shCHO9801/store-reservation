package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Store 엔티티 저장 및 조회 테스트")
    void saveAndName() {
        //given
        User owner = User.builder()
                .username("owner")
                .password("password")
                .role("PARTNER")
                .build();
        userRepository.save(owner);

        Store store = Store.builder()
                .name("test")
                .location("123 street")
                .description("test store description")
                .owner(owner)
                .build();

        //when
        storeRepository.save(store);
        Optional<Store> foundStore = storeRepository.findById(store.getId());

        //then
        assertTrue(foundStore.isPresent());
        assertEquals(store.getName(), foundStore.get().getName());
        assertEquals(store.getLocation(), foundStore.get().getLocation());
        assertEquals(store.getDescription(), foundStore.get().getDescription());
        assertEquals(store.getOwner(), foundStore.get().getOwner());
    }

}