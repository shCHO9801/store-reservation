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
import com.zerobase.storereservation.security.UserDetailsImpl;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.zerobase.storereservation.exception.ErrorCode.*;

/**
 * StoreService
 * - 매장 관리 비즈니스 로직을 처리하는 서비스
 * - 매장 생성, 조회, 수정, 삭제, 평점 업데이트, 정렬 기능 제공
 */
@Service
@RequiredArgsConstructor
public class StoreService {

    // 매장 관련 데이터 작업을 처리하는 Repository
    private final StoreRepository storeRepository;

    // 사용자 관련 데이터 작업을 처리하는 Repository
    private final UserRepository userRepository;

    // 리뷰 관련 데이터 작업을 처리하는 Repository
    private final ReviewRepository reviewRepository;

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 매장의 평균 평점을 업데이트
     * - 해당 매장의 모든 리뷰를 기반으로 평균 평점을 계산하고 저장
     *
     * @param storeId 매장 ID
     */
    public void updateAverageRating(Long storeId) {
        loggingUtil.logRequest("UPDATE AVERAGE RATING", storeId);

        List<Review> reviews = reviewRepository.findByStoreId(storeId);

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        store.setAverageRating(averageRating);
        storeRepository.save(store);

        loggingUtil.logSuccess(
                "UPDATE AVERAGE RATING",
                "매장 ID: " + storeId + ", 평균 평점: " + averageRating);
    }

    /**
     * 매장 생성
     * - 매장 정보를 생성하고 저장
     *
     * @param request 매장 생성 요청 DTO
     * @return 생성된 매장 정보 DTO
     */
    public StoreDto.Response createStore(StoreDto.CreateRequest request) {
        loggingUtil.logRequest("CREATE STORE", request);

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Store store = Store.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        store = storeRepository.save(store);

        StoreDto.Response response = convertToDto(store);

        loggingUtil.logSuccess("CREATE STORE", response);
        return response;
    }

    /**
     * 매장 ID로 매장 정보 조회
     *
     * @param id 매장 ID
     * @return 매장 정보 DTO
     */
    public StoreDto.Response getStoreById(Long id) {
        loggingUtil.logRequest("GET STORE BY ID", id);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        StoreDto.Response response = convertToDto(store);

        loggingUtil.logSuccess("GET STORE BY ID", response);
        return response;
    }

    /**
     * 매장 정보 수정
     *
     * @param id      매장 id
     * @param request 매장 수정 요청 DTO
     * @return 수정된 매장 정보 DTO
     */
    public StoreDto.Response updateStore(Long id, StoreDto.CreateRequest request) {
        loggingUtil.logRequest("UPDATE STORE", request);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        validateOwnership(store);

        store.setName(request.getName());
        store.setDescription(request.getDescription());
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());

        storeRepository.save(store);

        StoreDto.Response response = convertToDto(store);

        loggingUtil.logSuccess("UPDATE STORE", response);
        return response;
    }

    /**
     * 매장 삭제
     *
     * @param id 매장 ID
     */
    public void deleteStore(Long id) {
        loggingUtil.logRequest("DELETE STORE", id);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        validateOwnership(store);

        storeRepository.delete(store);

        loggingUtil.logSuccess("DELETE STORE", "매장 ID" + id);
    }

    /**
     * 메징 목록 조회
     * - 정렬 기준에 따라 매장 목록 반환
     *
     * @param sortBy  정렬 기준 (name, rating, distance 중 하나)
     * @param userLat 사용자 위도 (거리 정렬 시 필요)
     * @param userLon 사용자 경도 (거리 정렬 시 필요)
     * @return 정렬된 매장 목록 DTO
     */
    public List<StoreDto.Response> getStores(
            String sortBy, Double userLat, Double userLon
    ) {
        loggingUtil.logRequest("GET STORES", sortBy, userLat, userLon);

        if (!List.of("name", "rating", "distance").contains(sortBy)) {
            throw new CustomException(INVALID_CRITERIA);
        }

        if ("distance".equals(sortBy)) {
            if (userLat == null || userLon == null) {
                throw new CustomException(ErrorCode.INVALID_LOCATION);
            }
        }

        List<Store> stores = storeRepository.findAll();

        List<StoreDto.Response> responses = stores.stream()
                .map(store -> {
                    double distance = "distance".equals(sortBy) ?
                            calculateDistance(
                                    userLat, userLon, store.getLatitude(),
                                    store.getLongitude()) : 0.0;
                    return convertToDto(store, distance);
                }).collect(Collectors.toList());

        switch (sortBy) {
            case "name":
                responses.sort(Comparator.comparing(StoreDto.Response::getName));
                break;
            case "rating":
                responses.sort(Comparator
                        .comparingDouble(StoreDto.Response::getAverageRating)
                        .reversed()
                );
                break;
            case "distance":
                responses.sort(Comparator
                        .comparingDouble(StoreDto.Response::getDistance)
                );
                break;
        }

        loggingUtil.logSuccess("GET STORES", "정렬 기준: " + sortBy + ", 매장 수: " + responses.size());
        return responses;
    }

    // ==== Private Helper Methods ====

    /**
     * 거리 계산
     *
     * @param lat1 사용자 위도
     * @param lon1 사용자 경도
     * @param lat2 매장 위도
     * @param lon2 매장 경도
     * @return 계산된 거리 (km 단위)
     */
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

    /**
     * 소유권 확인
     * - 현재 사용자가 매장의 소유자인지 확인
     *
     * @param store 매장 엔티티
     * @throws CustomException 소유자가 아닌 경우
     */
    private void validateOwnership(Store store) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        User currentUser = userDetails.getUser();

        if (!store.getOwner().getId().equals(currentUser.getId())) {
            throw new CustomException(UNAUTHORIZED_ACTION);
        }
    }

    /**
     * 매장 엔티티를 Response DTO 로 변환
     *
     * @param store    매장 엔티티
     * @param distance 계산된 거리 (기본값 0.0)
     * @return 변환된 매장 Response DTO
     */
    private StoreDto.Response convertToDto(Store store, double distance) {
        return StoreDto.Response.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .ownerId(store.getOwner().getId())
                .averageRating(store.getAverageRating())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .distance(distance)
                .build();
    }

    /**
     * 매장 엔티티를 기본 Response DTO 로 변환
     *
     * @param store 매장 엔티티
     * @return 변환된 매장 Response DTO
     */
    private StoreDto.Response convertToDto(Store store) {
        return convertToDto(store, 0.0);
    }
}
