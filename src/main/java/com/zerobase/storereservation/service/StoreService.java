package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.StoreDto;
import com.zerobase.storereservation.entity.Review;
import com.zerobase.storereservation.entity.Store;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
import com.zerobase.storereservation.repository.ReviewRepository;
import com.zerobase.storereservation.repository.StoreRepository;
import com.zerobase.storereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.zerobase.storereservation.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public void updateAverageRating(Long storeId) {
        List<Review> reviews = reviewRepository.findByStoreId(storeId);

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        store.setAverageRating(averageRating);
        storeRepository.save(store);
    }

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
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        return StoreDto.Response.builder()
                .id(store.getId())
                .name(store.getName())
                .location(store.getLocation())
                .description(store.getDescription())
                .ownerId(store.getOwner().getId())
                .build();
    }

    public StoreDto.Response updateStore(Long id, StoreDto.CreateRequest request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        validateOwnership(store);

        store.setName(request.getName());
        store.setLocation(request.getLocation());
        store.setDescription(request.getDescription());

        storeRepository.save(store);
        return StoreDto.Response.builder()
                .id(store.getId())
                .name(store.getName())
                .location(store.getLocation())
                .description(store.getDescription())
                .ownerId(store.getOwner().getId())
                .build();
    }

    public void deleteStore(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        validateOwnership(store);

        storeRepository.delete(store);
    }

    public List<StoreDto.Response> getStores(
            String sortBy, Double userLat, Double userLon
    ) {
        if (!List.of("name", "rating", "distance").contains(sortBy)) {
            throw new CustomException(INVALID_CRITERIA);
        }

        if ("distance".equals(sortBy)) {
            if (userLat == null || userLon == null) {
                throw new CustomException(ErrorCode.INVALID_LOCATION);
            }
        }

        List<Store> stores = storeRepository.findAll();

        if ("distance".equals(sortBy)) {
            stores.forEach(store -> {
                Double distance = calculateDistance(userLat, userLon,
                        Double.parseDouble(store.getLocation().split(",")[0]),
                        Double.parseDouble(store.getLocation().split(",")[1])
                );
                store.setDistance(distance);
            });
            storeRepository.saveAll(stores);
        }

        switch (sortBy) {
            case "name":
                stores.sort(Comparator.comparing(Store::getName));
                break;
            case "rating":
                stores.sort(Comparator.comparingDouble(Store::getAverageRating).reversed());
                break;
            case "distance":
                stores.sort(Comparator.comparingDouble(Store::getDistance));
                break;
        }

        return stores.stream()
                .map(store -> StoreDto.Response.builder()
                        .id(store.getId())
                        .name(store.getName())
                        .location(store.getLocation())
                        .description(store.getDescription())
                        .ownerId(store.getOwner().getId())
                        .averageRating(store.getAverageRating())
                        .distance(store.getDistance())
                        .build()
                ).collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void validateOwnership(Store store) {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        if (!store.getOwner().getId().equals(currentUser.getId())) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }
    }
}
