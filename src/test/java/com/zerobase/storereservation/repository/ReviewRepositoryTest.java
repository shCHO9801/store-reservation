package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.Review;
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
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Review 엔티티 저장 및 조회 서비스")
    void saveAndFindReview() {
        //given
        User user = User.builder()
                .username("reviewer")
                .password("password")
                .role("CUSTOMER")
                .build();
        userRepository.save(user);

        Store store = Store.builder()
                .name("Review Store")
                .location("test location")
                .description("test description")
                .owner(user)
                .build();
        storeRepository.save(store);

        Review review = Review.builder()
                .store(store)
                .user(user)
                .content("This is a review")
                .createdAt(LocalDateTime.now())
                .build();

        //when
        reviewRepository.save(review);
        Optional<Review> foundReview = reviewRepository.findById(review.getId());

        //then
        assertTrue(foundReview.isPresent());
        assertEquals(review.getStore().getId(), foundReview.get().getStore().getId());
        assertEquals(review.getUser().getId(), foundReview.get().getUser().getId());
        assertEquals(review.getContent(), foundReview.get().getContent());
        assertEquals(review.getCreatedAt(), foundReview.get().getCreatedAt());

    }

}