package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.ReviewDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Review;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.entity.constants.ReservationStatus;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.ReservationRepository;
import com.zerobase.storereservation.repository.ReviewRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.zerobase.storereservation.entity.constants.ReservationStatus.CONFIRMED;
import static com.zerobase.storereservation.entity.constants.Role.CUSTOMER;
import static com.zerobase.storereservation.entity.constants.Role.PARTNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Store store;
    private User user;
    private Reservation reservation;

    @Autowired
    private StoreService storeService;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(
                User.builder()
                        .username("Store owner")
                        .password("ownerpassword")
                        .role(PARTNER)
                        .build()
        );

        user = userRepository.save(
                User.builder()
                        .username("Test User")
                        .password("password")
                        .role(CUSTOMER)
                        .build()
        );

        store = storeRepository.save(
                Store.builder()
                        .name("Test Store")
                        .location("123, 456")
                        .description("Test Description")
                        .averageRating(0.0)
                        .owner(owner)
                        .build()
        );
        reservation = reservationRepository.save(
                Reservation.builder()
                        .user(user)
                        .store(store)
                        .phoneNumber("010-1234-5678")
                        .status(CONFIRMED)
                        .reservedAt(LocalDateTime.now())
                        .build()
        );
    }

    @Test
    @DisplayName("리뷰 등록 후 평균 평점 업데이트")
    void createReviewAndUpdateAverageRating() {
        //given
        ReviewDto.CreateRequest request1 = new ReviewDto.CreateRequest();
        request1.setStoreId(store.getId());
        request1.setUserId(user.getId());
        request1.setRating(4);
        request1.setContent("Great place!");
        reviewService.createReview(request1);

        ReviewDto.CreateRequest request2 = new ReviewDto.CreateRequest();
        request2.setStoreId(store.getId());
        request2.setUserId(user.getId());
        request2.setRating(5);
        request2.setContent("Amazing experience!");
        reviewService.createReview(request2);

        //when
        Store updatedStore = storeRepository.findById(store.getId())
                .orElseThrow();

        //then
        assertEquals(4.5, updatedStore.getAverageRating());
    }

    @Test
    @DisplayName("리뷰 삭제 후 평균 평점 업데이트")
    void deleteReviewAndUpdateAverageRating() {
        //given
        ReviewDto.CreateRequest request1 = new ReviewDto.CreateRequest();
        request1.setStoreId(store.getId());
        request1.setUserId(user.getId());
        request1.setRating(4);
        request1.setContent("Great place!");
        reviewService.createReview(request1);

        ReviewDto.CreateRequest request2 = new ReviewDto.CreateRequest();
        request2.setStoreId(store.getId());
        request2.setUserId(user.getId());
        request2.setRating(5);
        request2.setContent("Amazing experience!");
        reviewService.createReview(request2);

        //when
        reviewService.deleteReview(1L, user.getId());
        Store updatedStore = storeRepository.findById(store.getId())
                .orElseThrow();

        //then
        assertEquals(5.0, updatedStore.getAverageRating());
    }

    @Test
    @DisplayName("리뷰가 없는 경우 평균 평점은 0")
    void averageRatingWhenNoReviews() {
        //given
        ReviewDto.CreateRequest request = new ReviewDto.CreateRequest();
        request.setStoreId(store.getId());
        request.setUserId(user.getId());
        request.setRating(4);
        request.setContent("Great place!");
        ReviewDto.Response createdReview = reviewService.createReview(request);

        //when
        reviewService.deleteReview(createdReview.getId(), user.getId());
        Store updateStore = storeRepository.findById(store.getId())
                .orElseThrow();

        //then
        assertEquals(0.0, updateStore.getAverageRating());
    }

    @Test
    @DisplayName("여러 사용자의 리뷰 평균 평점 계산")
    void multipleUserReview() {
        //given
        User anotherUser = userRepository.save(
                User.builder()
                        .username("another User")
                        .password("password")
                        .role(CUSTOMER)
                        .build()
        );

        reservationRepository.save(
                Reservation.builder()
                        .user(anotherUser)
                        .store(store)
                        .phoneNumber("010-1234-5678")
                        .status(CONFIRMED)
                        .reservedAt(LocalDateTime.now())
                        .build()
        );

        ReviewDto.CreateRequest request1 = new ReviewDto.CreateRequest();
        request1.setStoreId(store.getId());
        request1.setUserId(user.getId());
        request1.setRating(3);
        request1.setContent("Great place!");
        reviewService.createReview(request1);

        ReviewDto.CreateRequest request2 = new ReviewDto.CreateRequest();
        request2.setStoreId(store.getId());
        request2.setUserId(anotherUser.getId());
        request2.setRating(5);
        request2.setContent("Amazing experience!");
        reviewService.createReview(request2);

        //when
        Store updatedStore = storeRepository.findById(store.getId())
                .orElseThrow();

        //then
        assertEquals(4.0, updatedStore.getAverageRating());
    }

    @Test
    @DisplayName("평점 범위 유효성 검증")
    void validateRatingRange() {
        //given
        ReviewDto.CreateRequest request = new ReviewDto.CreateRequest();
        request.setStoreId(store.getId());
        request.setUserId(user.getId());
        request.setRating(6);
        request.setContent("Invalid rating test");

        //when&then
        assertThrows(CustomException.class,
                () -> reviewService.createReview(request));
    }
}
