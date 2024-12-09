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
@DisplayName("StoreService Test")
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
    @DisplayName("상점 등록 - 성공")
    void createStoreSuccess() {
        // given
        StoreDto.CreateRequest request = createStoreRequest();
        User mockUser = User.builder().id(1L).build();
        Store mockStore = createMockStore(mockUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

        // when
        StoreDto.Response response = storeService.createStore(request);

        // then
        assertNotNull(response);
        assertEquals("Test Store", response.getName());
        verify(userRepository, times(1)).findById(1L);
        verify(storeRepository, times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("상점 등록 - 실패: 소유자 없음")
    void createStoreUserNotFound() {
        // given
        StoreDto.CreateRequest request = createStoreRequest();
        request.setOwnerId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.createStore(request));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(storeRepository, never()).save(any());
    }

    @Test
    @DisplayName("상점 조회 - 성공")
    void getStoreByIdSuccess() {
        // given
        Long storeId = 1L;
        Store mockStore = createMockStore(User.builder().id(1L).build());

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

        // when
        StoreDto.Response response = storeService.getStoreById(storeId);

        // then
        assertNotNull(response);
        assertEquals("Test Store", response.getName());
        verify(storeRepository, times(1)).findById(storeId);
    }

    @Test
    @DisplayName("상점 조회 - 실패: 상점 없음")
    void getStoreByIdNotFound() {
        // given
        Long storeId = 999L;
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.getStoreById(storeId));
        assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("상점 수정 - 성공")
    void updateStoreSuccess() {
        // given
        Long storeId = 1L;
        StoreDto.CreateRequest request = createStoreRequest();
        Store mockStore = createMockStore(User.builder().id(1L).build());

        mockSecurityContext(mockStore.getOwner());

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        StoreDto.Response response = storeService.updateStore(storeId, request);

        // then
        assertNotNull(response);
        assertEquals("Test Store", response.getName());
    }

    @Test
    @DisplayName("상점 수정 - 실패: 권한 없음")
    void updateStoreUnauthorized() {
        // given
        Long storeId = 1L;
        StoreDto.CreateRequest request = createStoreRequest();
        Store mockStore = createMockStore(User.builder().id(1L).build());

        mockSecurityContext(User.builder().id(2L).build());

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.updateStore(storeId, request));
        assertEquals(ErrorCode.UNAUTHORIZED_ACTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("상점 삭제 - 성공")
    void deleteStoreSuccess() {
        // given
        Long storeId = 1L;
        Store mockStore = createMockStore(User.builder().id(1L).build());

        mockSecurityContext(mockStore.getOwner());
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

        // when
        storeService.deleteStore(storeId);

        // then
        verify(storeRepository, times(1)).delete(mockStore);
    }

    @Test
    @DisplayName("상점 삭제 - 실패: 권한 없음")
    void deleteStoreUnauthorized() {
        // given
        Long storeId = 1L;
        Store mockStore = createMockStore(User.builder().id(1L).build());

        mockSecurityContext(User.builder().id(2L).build());
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.deleteStore(storeId));
        assertEquals(ErrorCode.UNAUTHORIZED_ACTION, exception.getErrorCode());
    }

    // === Helper Methods ===

    private StoreDto.CreateRequest createStoreRequest() {
        return StoreDto.CreateRequest
                .builder()
                .name("Test Store")
                .description("Test Description")
                .latitude(37.7749)
                .longitude(-122.4194)
                .ownerId(1L)
                .build();
    }

    private Store createMockStore(User owner) {
        return Store.builder()
                .id(1L)
                .name("Test Store")
                .description("Test Description")
                .owner(owner)
                .latitude(37.7749)
                .longitude(-122.4194)
                .averageRating(4.0)
                .build();
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