package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.entity.constants.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("User 엔티티 저장 및 조회 테스트")
    void saveAndFindUser() {
        //given
        User user = User.builder()
                .username("test")
                .password("testPassword")
                .role(Role.CUSTOMER)
                .build();

        //when
        userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(user.getId());

        //then
        assertTrue(foundUser.isPresent());
        assertEquals(user.getUsername(), foundUser.get().getUsername());
        assertEquals(user.getPassword(), foundUser.get().getPassword());
        assertEquals(user.getRole(), foundUser.get().getRole());
    }

}