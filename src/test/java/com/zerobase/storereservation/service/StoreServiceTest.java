package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.StoreDto;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.entity.constants.Role;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import com.zerobase.storereservation.security.UserDetailsImpl;
import com.zerobase.storereservation.util.LoggingUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoggingUtil loggingUtil;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("상점 등록 성공")
    void createStoreSuccess() {
        //given
        StoreDto.CreateRequest request = new StoreDto.CreateRequest();
        request.setName("Test Store");
        request.setDescription("Test Description");
        request.setOwnerId(1L);
        request.setLatitude(37.7749);
        request.setLongitude(-122.4194);

        User mockUser = User.builder().id(1L).build();

        Store mockStore = Store.builder()
                .id(1L)
                .name("Test Store")
                .description("Test Description")
                .owner(mockUser)
                .averageRating(0.0)
                .latitude(37.7749)
                .longitude(-122.4194)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

        //when
        StoreDto.Response response = storeService.createStore(request);

        //then
        assertNotNull(response);
        assertEquals("Test Store", response.getName());
        assertEquals("Test Description", response.getDescription());
        assertEquals(37.7749, response.getLatitude());
        assertEquals(-122.4194, response.getLongitude());
        verify(userRepository, times(1)).findById(1L);
        verify(storeRepository, times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("상점 등록 실패 - 소유자 없음")
    void createStoreUserNotFound() {
        //given
        StoreDto.CreateRequest request = new StoreDto.CreateRequest();
        request.setOwnerId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.createStore(request));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(storeRepository, never()).save(any());
    }

    @Test
    @DisplayName("상점 조회 성공")
    void getStoreByIdSuccess() {
        //given
        Long storeId = 1L;
        User mockUser = User.builder().id(1L).build();
        Store mockStore = Store.builder()
                .id(storeId)
                .name("Test Store")
                .description("Test Description")
                .owner(mockUser)
                .averageRating(4.5)
                .latitude(37.7749)
                .longitude(-122.4194)
                .build();

        when(storeRepository.findById(storeId))
                .thenReturn(Optional.of(mockStore));

        //when
        StoreDto.Response response = storeService.getStoreById(storeId);

        //then
        assertNotNull(response);
        assertEquals("Test Store", response.getName());
        verify(storeRepository, times(1)).findById(storeId);
    }

    @Test
    @DisplayName("상점 조회 실패 - 상점 없음")
    void getStoreByIdNotFound() {
        //given
        Long storeId = 999L;
        when(storeRepository.findById(999L)).thenReturn(Optional.empty());

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.getStoreById(storeId));
        assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
        verify(storeRepository, times(1)).findById(storeId);
    }

    @Test
    @DisplayName("상점 수정 성공")
    void updateStoreSuccess() {
        //given
        Long storeId = 1L;
        StoreDto.CreateRequest request = new StoreDto.CreateRequest();
        request.setName("Update Store");
        request.setDescription("Update Description");
        request.setLatitude(40.7128);
        request.setLongitude(-74.0060);

        User mockUser = User.builder()
                .id(1L)
                .username("testusername")
                .password("testpassword")
                .role(Role.PARTNER)
                .build();
        Store mockStore = Store.builder()
                .id(storeId)
                .name("Original Store")
                .description("Original Description")
                .owner(mockUser)
                .averageRating(4.0)
                .latitude(37.7749)
                .longitude(-122.4194)
                .build();

        mockSecurityContext(mockUser);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
        when(storeRepository.save(any(Store.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //when
        StoreDto.Response response = storeService.updateStore(storeId, request);

        //then
        assertNotNull(response);
        assertEquals("Update Store", response.getName());
        assertEquals("Update Description", response.getDescription());
        assertEquals(40.7128, response.getLatitude());
        assertEquals(-74.0060, response.getLongitude());
        verify(storeRepository, times(1)).findById(storeId);
        verify(storeRepository, times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("상점 수정 실패 - 권한 없음")
    void updateStoreUnauthorized() {
        //given
        Long storeId = 1L;
        StoreDto.CreateRequest request = new StoreDto.CreateRequest();
        request.setName("Unauthorized Update");

        User mockOwner = User.builder().id(1L).build();
        User mockUser = User.builder().id(2L).build();

        Store mockStore = Store.builder()
                .id(storeId)
                .name("Original Store")
                .owner(mockOwner)
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
        mockSecurityContext(mockUser);

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.updateStore(storeId, request));
        assertEquals(ErrorCode.UNAUTHORIZED_ACTION, exception.getErrorCode());
        verify(storeRepository, times(1)).findById(storeId);
        verify(storeRepository, never()).save(any());
    }

    @Test
    @DisplayName("상점 삭제 성공")
    void deleteStoreSuccess() {
        //given
        Long storeId = 1L;
        User mockUser = User.builder().id(1L).build();
        Store mockStore = Store.builder()
                .id(storeId)
                .name("Test Store")
                .owner(mockUser)
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
        mockSecurityContext(mockUser);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

        //when
        storeService.deleteStore(storeId);

        //then
        verify(storeRepository, times(1)).findById(storeId);
        verify(storeRepository, times(1)).delete(mockStore);
    }


    @Test
    void deleteStoreUnauthorized() {
        //given
        Long storeId = 1L;
        User mockOwner = User.builder().id(1L).build();
        User mockUser = User.builder().id(2L).build();

        Store mockStore = Store.builder()
                .id(storeId)
                .name("Test Store")
                .owner(mockOwner)
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
        mockSecurityContext(mockUser);

        //when&then
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.deleteStore(storeId));
        assertEquals(ErrorCode.UNAUTHORIZED_ACTION, exception.getErrorCode());
        verify(storeRepository, times(1)).findById(storeId);
        verify(storeRepository, never()).delete(any());
    }

    private void mockSecurityContext(User mockUser) {
        UserDetailsImpl userDetails = new UserDetailsImpl(mockUser);
        Authentication mockAuth = mock(Authentication.class);
        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuth);
        when(mockAuth.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(mockSecurityContext);
    }
}