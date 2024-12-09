package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.ReviewDto;
import com.zerobase.storereservation.entity.Reservation;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.ReservationRepository;
import com.zerobase.storereservation.repository.ReviewRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import com.zerobase.storereservation.util.LoggingUtil;
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
import static org.junit.jupiter.api.Assertions.*;

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
                        .description("Test Description")
                        .averageRating(0.0)
                        .latitude(123.0)
                        .longitude(456.0)
                        .owner(owner)
                        .build()
        );

        reservation = reservationRepository.saveAndFlush(
                Reservation.builder()
                        .user(user)
                        .store(store)
                        .phoneNumber("010-1234-5678")
                        .status(CONFIRMED)
                        .reservedAt(LocalDateTime.now())
                        .build()
        );
    }

    private ReviewDto.CreateRequest createReviewRequest(Long storeId, Long userId, int rating, String content) {
        ReviewDto.CreateRequest request = new ReviewDto.CreateRequest();
        request.setStoreId(storeId);
        request.setUserId(userId);
        request.setRating(rating);
        request.setContent(content);
        return request;
    }

    @Test
    @DisplayName("리뷰 등록 - 평균 평점이 올바르게 업데이트되는지 검증")
    void shouldUpdateAverageRatingWhenReviewCreated() {
        //given
        ReviewDto.CreateRequest request1 = createReviewRequest(store.getId(), user.getId(), 4, "Great place!");
        ReviewDto.CreateRequest request2 = createReviewRequest(store.getId(), user.getId(), 5, "Amazing experience!");

        reviewService.createReview(request1);
        reviewService.createReview(request2);

        //when
        Store updatedStore = storeRepository.findById(store.getId())
                .orElseThrow();

        //then
        assertEquals(4.5, updatedStore.getAverageRating());
    }

    @Test
    @DisplayName("리뷰 삭제 - 평균 평점이 올바르게 업데이트되는지 검증")
    void shouldUpdateAverageRatingWhenReviewDeleted() {
        //given
        ReviewDto.CreateRequest request1 = createReviewRequest(store.getId(), user.getId(), 4, "Great place!");
        ReviewDto.CreateRequest request2 = createReviewRequest(store.getId(), user.getId(), 5, "Amazing experience!");

        ReviewDto.Response review = reviewService.createReview(request1);
        reviewService.createReview(request2);

        //when
        reviewService.deleteReview(review.getId(), user.getId());
        Store updatedStore = storeRepository.findById(store.getId())
                .orElseThrow();

        //then
        assertEquals(5.0, updatedStore.getAverageRating());
    }

    @Test
    @DisplayName("리뷰가 없는 경우 - 평균 평점은 0이 되어야 함")
    void shouldSetAverageRatingToZeroWhenNoReviewsExist() {
        //given
        ReviewDto.CreateRequest request = createReviewRequest(store.getId(), user.getId(), 4, "Great place!");
        ReviewDto.Response createdReview = reviewService.createReview(request);

        //when
        reviewService.deleteReview(createdReview.getId(), user.getId());
        Store updatedStore = storeRepository.findById(store.getId())
                .orElseThrow();

        //then
        assertEquals(0.0, updatedStore.getAverageRating());
    }

    @Test
    @DisplayName("여러 사용자의 리뷰 등록 - 평균 평점이 올바르게 계산되는지 검증")
    void shouldCalculateAverageRatingForMultipleUsers() {
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

        ReviewDto.CreateRequest request1 = createReviewRequest(store.getId(), user.getId(), 3, "Great place!");
        ReviewDto.CreateRequest request2 = createReviewRequest(store.getId(), anotherUser.getId(), 5, "Amazing experience!");

        reviewService.createReview(request1);
        reviewService.createReview(request2);

        //when
        Store updatedStore = storeRepository.findById(store.getId())
                .orElseThrow();

        //then
        assertEquals(4.0, updatedStore.getAverageRating());
    }

    @Test
    @DisplayName("리뷰 등록 - 평점 범위(1~5)를 벗어난 경우 예외 처리 검증")
    void shouldThrowExceptionWhenRatingOutOfRange() {
        //given
        ReviewDto.CreateRequest request = createReviewRequest(store.getId(), user.getId(), 6, "Invalid rating test");

        //when&then
        assertThrows(CustomException.class,
                () -> reviewService.createReview(request));
    }
}