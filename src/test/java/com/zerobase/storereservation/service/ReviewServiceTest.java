package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.ReviewDto;
import com.zerobase.storereservation.entity.Review;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
import com.zerobase.storereservation.repository.ReviewRepository;
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

import static com.zerobase.storereservation.exception.ErrorCode.REVIEW_NOT_FOUND;
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
                .id(1L)
                .name("Test Store")
                .build();

        User user = User.builder()
                .id(1L)
                .username("Test User")
                .build();

        when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        ReviewDto.CreateRequest request =
                new ReviewDto.CreateRequest();
        request.setStoreId(1L);
        request.setUserId(1L);
        request.setContent("Great Place");

        Review savedReview = Review.builder()
                .id(1L)
                .store(store)
                .user(user)
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewRepository.save(any())).thenReturn(savedReview)
                .thenReturn(savedReview);

        //when
        ReviewDto.Response response =
                reviewService.createReview(request);

        //then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getStoreId());
        assertEquals(1L, response.getUserId());
        assertEquals(savedReview.getContent(), response.getContent());
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
    @DisplayName("리뷰 삭제 성공")
    void deleteReviewSuccess() {
        //given
        when(reviewRepository.existsById(1L)).thenReturn(true);

        //when
        reviewService.deleteReview(1L);

        //then
        verify(reviewRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("삭제할 리뷰가 없는 경우")
    void deleteReviewNotFound() {
        //given
        when(reviewRepository.findById(1L))
                .thenReturn(Optional.empty());

        //then
        CustomException exception = assertThrows(
                CustomException.class, () -> reviewService.deleteReview(1L)
        );
        assertEquals(REVIEW_NOT_FOUND, exception.getErrorCode());
    }
}