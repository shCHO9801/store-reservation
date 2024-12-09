package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.ReviewDto;
import com.zerobase.storereservation.entity.Review;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
import com.zerobase.storereservation.repository.ReservationRepository;
import com.zerobase.storereservation.repository.ReviewRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import com.zerobase.storereservation.util.LoggingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.zerobase.storereservation.entity.constants.ReservationStatus.CONFIRMED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StoreService storeService;

    @Mock
    private LoggingUtil loggingUtil;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("리뷰 등록 성공")
    void createReviewSuccess() {
        //given
        Store store = Store.builder()
                .id(1L).name("Test Store").build();

        User user = User.builder()
                .id(1L).username("Test User").build();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationRepository.existsByUserIdAndStoreIdAndStatus(
                1L, 1L, CONFIRMED
        )).thenReturn(true);
        doNothing().when(storeService).updateAverageRating(1L);

        ReviewDto.CreateRequest request =
                new ReviewDto.CreateRequest();
        request.setStoreId(1L);
        request.setUserId(1L);
        request.setRating(3);
        request.setContent("Great Place");

        Review savedReview = Review.builder()
                .id(1L)
                .store(store)
                .user(user)
                .content(request.getContent())
                .rating(request.getRating())
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewRepository.save(any())).thenReturn(savedReview);

        //when
        ReviewDto.Response response = reviewService.createReview(request);

        //then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getStoreId());
        assertEquals(1L, response.getUserId());
        assertEquals(savedReview.getContent(), response.getContent());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 유효하지 않은 평점")
    void createReviewInvalidRating() {
        //given
        ReviewDto.CreateRequest request = new ReviewDto.CreateRequest();
        request.setStoreId(1L);
        request.setUserId(1L);
        request.setContent("Invalid rating test");
        request.setRating(6);

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.createReview(request));
        assertEquals(ErrorCode.INVALID_RATING, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 예약 없음")
    void createReviewFailNoReservation() {
        //given
        when(reservationRepository.existsByUserIdAndStoreIdAndStatus(
                1L, 1L, CONFIRMED
        )).thenReturn(false);

        ReviewDto.CreateRequest request = new ReviewDto.CreateRequest();
        request.setStoreId(1L);
        request.setUserId(1L);
        request.setContent("Great Place");
        request.setRating(3);

        //when&then
        CustomException e = assertThrows(CustomException.class,
                () -> reviewService.createReview(request));
        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 존재하지 않는 매장")
    void createReviewStoreNotFound() {
        //given
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        ReviewDto.CreateRequest request = new ReviewDto.CreateRequest();
        request.setStoreId(1L);
        request.setUserId(1L);
        request.setRating(3);
        request.setContent("Store not found test");

        when(reservationRepository.existsByUserIdAndStoreIdAndStatus(
                1L, 1L, CONFIRMED
        )).thenReturn(true);

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.createReview(request));
        assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 존재하지 않는 사용자")
    void createReviewUserNotFound() {
        //given
        Store store = Store.builder().id(1L).build();
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ReviewDto.CreateRequest request = new ReviewDto.CreateRequest();
        request.setStoreId(1L);
        request.setUserId(1L);
        request.setRating(3);
        request.setContent("User not found test");
        when(reservationRepository.existsByUserIdAndStoreIdAndStatus(
                1L, 1L, CONFIRMED
        )).thenReturn(true);

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.createReview(request));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 조회 성공")
    void getReviewsByStoreSuccess() {
        //givne
        Store store = Store.builder()
                .id(1L)
                .name("Test Store")
                .build();

        User user = User.builder()
                .id(1L)
                .username("Test User")
                .build();

        Review review1 = Review.builder()
                .id(1L)
                .store(store)
                .user(user)
                .content("Great Place")
                .createdAt(LocalDateTime.now())
                .build();

        Review review2 = Review.builder()
                .id(2L)
                .store(store)
                .user(user)
                .content("Would visit again!")
                .createdAt(LocalDateTime.now())
                .build();

        when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));
        when(reviewRepository.findByStoreId(1L))
                .thenReturn(List.of(review1, review2));

        //when
        List<ReviewDto.Response> responses =
                reviewService.getReviewsByStore(1L);

        //then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Great Place", responses.get(0).getContent());
        assertEquals("Would visit again!", responses.get(1).getContent());
    }

    @Test
    @DisplayName("리뷰 삭제 성공 - 작성자가 삭제")
    void deleteReviewSuccessByReviewer() {
        //given
        Store store = Store.builder()
                .id(1L)
                .owner(User.builder().id(2L).build())
                .build();

        User user = User.builder()
                .id(1L)
                .username("Reviewer")
                .build();

        Review review = Review.builder()
                .id(1L)
                .store(store)
                .user(user)
                .content("Great Place")
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        //when
        reviewService.deleteReview(1L, 1L);

        //then
        verify(reviewRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("리뷰 삭제 성공 - 매장 소유자가 삭제")
    void deleteReviewSuccessByOwner() {
        //given
        Store store = Store.builder()
                .id(1L)
                .owner(User.builder().id(2L).build())
                .build();

        Review review = Review.builder()
                .id(1L)
                .store(store)
                .user(User.builder().id(1L).build())
                .content("Great Place")
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        //when
        reviewService.deleteReview(1L, 2L);

        //then
        verify(reviewRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 권한 없음")
    void deleteReviewUnauthorized() {
        //given
        Store store = Store.builder()
                .id(1L)
                .owner(User.builder().id(2L).build())
                .build();

        Review review = Review.builder()
                .id(1L)
                .store(store)
                .user(User.builder().id(1L).build())
                .content("Great Place")
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.deleteReview(1L, 3L));
        assertEquals(ErrorCode.UNAUTHORIZED_ACTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReviewSuccess() {
        //given
        User user = User.builder().id(1L).build();

        Store store = Store.builder()
                .id(1L)
                .name("Test Store")
                .build();

        Review review = Review.builder()
                .id(1L)
                .user(user)
                .store(store)
                .content("Old Content")
                .rating(3)
                .createdAt(LocalDateTime.now())
                .build();


        ReviewDto.UpdateRequest request = new ReviewDto.UpdateRequest();
        request.setContent("Updated Content");
        request.setRating(5);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        //when
        ReviewDto.Response response =
                reviewService.updateReview(1L, 1L, request);

        //then
        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Updated Content", response.getContent());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 권한 없음")
    void updateReviewUnauthorized() {
        //given
        Review review = Review.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .store(Store.builder().id(1L).build())
                .content("Old Content")
                .createdAt(LocalDateTime.now())
                .build();

        ReviewDto.UpdateRequest request = new ReviewDto.UpdateRequest();
        request.setContent("Updated Content");
        request.setRating(5);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.updateReview(1L, 2L, request));
        assertEquals(ErrorCode.UNAUTHORIZED_ACTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 조회 성공")
    void getReviewByStoreSuccess() {
        //given
        Store store = Store.builder().id(1L).build();

        Review review1 = Review.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .store(store)
                .content("Great Place")
                .rating(5)
                .createdAt(LocalDateTime.now())
                .build();

        Review review2 = Review.builder()
                .id(2L)
                .user(User.builder().id(3L).build())
                .store(store)
                .content("test Review")
                .rating(3)
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewRepository.findByStoreId(1L)).thenReturn(List.of(review1, review2));

        //when
        List<ReviewDto.Response> responses = reviewService.getReviewsByStore(1L);

        //then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Great Place", responses.get(0).getContent());
        assertEquals(5, responses.get(0).getRating());
        assertEquals("test Review", responses.get(1).getContent());
        assertEquals(3, responses.get(1).getRating());
    }
}