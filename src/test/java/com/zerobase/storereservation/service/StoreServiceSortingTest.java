package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.StoreDto;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
import com.zerobase.storereservation.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreServiceSortingTest {

    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreRepository storeRepository;

    private List<Store> mockStores;

    @BeforeEach
    void setUp() {
        User owner = User.builder().id(1L).build();

        mockStores = Arrays.asList(
                Store.builder()
                        .id(1L)
                        .name("B Store")
                        .description("Great Place")
                        .averageRating(3.5)
                        .latitude(35.6895)
                        .longitude(139.6917)
                        .distance(0.0)
                        .owner(owner)
                        .build(),
                Store.builder()
                        .id(2L)
                        .name("A Store")
                        .description("Good!")
                        .averageRating(4.0)
                        .latitude(37.7749)
                        .longitude(-122.4194)
                        .distance(0.0)
                        .owner(owner)
                        .build(),
                Store.builder()
                        .id(3L)
                        .name("C Store")
                        .description("SoSo")
                        .averageRating(3.0)
                        .latitude(51.5074)
                        .longitude(-0.1278)
                        .distance(0.0)
                        .owner(owner)
                        .build()
        );
    }

    @Test
    @DisplayName("가나다순 정렬")
    void sortByName() {
        //given
        when(storeRepository.findAll()).thenReturn(mockStores);

        //when
        List<StoreDto.Response> result =
                storeService.getStores("name", null, null);

        //then
        assertEquals(3, result.size());
        assertEquals("A Store", result.get(0).getName());
        assertEquals("B Store", result.get(1).getName());
        assertEquals("C Store", result.get(2).getName());
    }

    @Test
    @DisplayName("별점순 정렬")
    void sortByRating() {
        //given
        when(storeRepository.findAll()).thenReturn(mockStores);

        //when
        List<StoreDto.Response> result =
                storeService.getStores("rating", null, null);

        //then
        assertEquals(3, result.size());
        assertEquals(4.0, result.get(0).getAverageRating());
        assertEquals(3.5, result.get(1).getAverageRating());
        assertEquals(3.0, result.get(2).getAverageRating());
    }

    @Test
    @DisplayName("거리순 정렬")
    void sortByDistance() {
        //given
        double userLat = 37.7749;
        double userLon = -122.4194;
        when(storeRepository.findAll()).thenReturn(mockStores);

        //when
        List<StoreDto.Response> result =
                storeService.getStores("distance", userLat, userLon);

        //then
        assertEquals(3, result.size());
        assertEquals("A Store", result.get(0).getName());
        assertEquals("B Store", result.get(1).getName());
        assertEquals("C Store", result.get(2).getName());
    }

    @Test
    @DisplayName("잘못된 정렬 기준으로 예외 발생")
    void sortByInvalidCriteria() {
        //given
        double userLat = 37.7749;
        double userLon = -122.4194;

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.getStores("invalidSort", userLat, userLon));
        assertEquals(ErrorCode.INVALID_CRITERIA, exception.getErrorCode());
        verify(storeRepository, never()).findAll();
    }

    @Test
    @DisplayName("위치 값이 유효하지 않을 때 예외 발생")
    void sortByDistanceInvalidLocation() {
        //given
        //when&then
        CustomException exception1 = assertThrows(CustomException.class,
                () -> storeService.getStores("distance", 37.7749, null));
        assertEquals(ErrorCode.INVALID_LOCATION, exception1.getErrorCode());

        CustomException exception2 = assertThrows(CustomException.class,
                () -> storeService.getStores("distance", null, null));
        assertEquals(ErrorCode.INVALID_LOCATION, exception2.getErrorCode());

        CustomException exception3 = assertThrows(CustomException.class,
                () -> storeService.getStores("distance", null, 37.7749));
        assertEquals(ErrorCode.INVALID_LOCATION, exception3.getErrorCode());

        verify(storeRepository, never()).findAll();
    }
}
