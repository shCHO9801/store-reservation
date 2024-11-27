package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.StoreDto;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.zerobase.storereservation.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public StoreDto.Response createStore(StoreDto.CreateRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Store store = Store.builder()
                .name(request.getName())
                .location(request.getLocation())
                .description(request.getDescription())
                .owner(owner)
                .build();

        store = storeRepository.save(store);

        return StoreDto.Response.builder()
                .id(store.getId())
                .name(store.getName())
                .location(store.getLocation())
                .description(store.getDescription())
                .ownerId(store.getOwner().getId())
                .build();
    }

    public StoreDto.Response getStoreById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        return StoreDto.Response.builder()
                .id(store.getId())
                .name(store.getName())
                .location(store.getLocation())
                .description(store.getDescription())
                .ownerId(store.getOwner().getId())
                .build();
    }
}
