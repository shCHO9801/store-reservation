package com.zerobase.storereservation.controller;

import com.zerobase.storereservation.dto.StoreDto;
import com.zerobase.storereservation.service.StoreService;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * StoreController
 * 매장 관리를 위한 컨트롤러
 * - 매장 생성, 수정, 삭제, 조회 기능 제공
 */
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    // 매장 관련 비즈니스 로직을 처리하는 서비스
    private final StoreService storeService;
    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 매장 생성
     * - 점주가 새로운 매장을 등록합니다.
     *
     * @param request 매장 생성 요청 DTO
     * @return 생성된 매장 정보
     */
    @PreAuthorize("hasRole('PARTNER')")
    @PostMapping
    public ResponseEntity<StoreDto.Response> createStore(
            @RequestBody StoreDto.CreateRequest request
    ) {
        loggingUtil.logRequest("CREATE STORE", request);
        StoreDto.Response response = storeService.createStore(request);
        loggingUtil.logSuccess("CREATE STORE", response);
        return ResponseEntity.ok(response);
    }

    /**
     * 매장 정보 수정
     * - 점주가 특정 매장의 정보를 수정합니다.
     *
     * @param id      매장 ID
     * @param request 매장 수정 요청 DTO
     * @return 수정된 매장 정보
     */
    @PreAuthorize("hasRole('PARTNER')")
    @PutMapping("/{id}")
    public ResponseEntity<StoreDto.Response> updateStore(
            @PathVariable Long id,
            @RequestBody StoreDto.CreateRequest request
    ) {
        loggingUtil.logRequest("UPDATE STORE", id, request);
        StoreDto.Response response = storeService.updateStore(id, request);
        loggingUtil.logSuccess("UPDATE STORE", response);
        return ResponseEntity.ok(response);
    }

    /**
     * 매장 삭제
     * - 점주가 특정 매장을 삭제합니다.
     *
     * @param id 매장 ID
     * @return 성공 상태 코드
     */
    @PreAuthorize("hasRole('PARTNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(
            @PathVariable Long id
    ) {
        loggingUtil.logSuccess("DELETE STORE", id);
        storeService.deleteStore(id);
        loggingUtil.logSuccess("DELETE STORE", "매장 ID: " + id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 매장 정보 조회
     * - 매장 ID를 사용하여 매장 정보를 조회합니다.
     *
     * @param id 매장 ID
     * @return 매장 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<StoreDto.Response> getStoreById(
            @PathVariable Long id) {
        loggingUtil.logRequest("GET STORE BY ID", id);
        StoreDto.Response response = storeService.getStoreById(id);
        loggingUtil.logSuccess("GET STORE BY ID", response);
        return ResponseEntity.ok(response);
    }

    /**
     * 매장 목록 조회
     * - 조건에 따라 매장 목록을 정렬하거나 사용자 위치를 기준으로 가까운 매장을 반환합니다.
     *
     * @param sortBy  정렬 기준 (기본값: name)
     * @param userLat 사용자 위도 (옵션)
     * @param userLon 사용자 경도 (옵션)
     * @return 매장 목록
     */
    @GetMapping
    public ResponseEntity<List<StoreDto.Response>> getStores(
            @RequestParam(required = false, defaultValue = "name")
            String sortBy,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon
    ) {
        loggingUtil.logRequest("GET STORES", sortBy, userLat, userLon);
        List<StoreDto.Response> stores =
                storeService.getStores(sortBy, userLat, userLon);
        loggingUtil.logSuccess("GET STORES", "정렬 기준: " + sortBy + ", 위치 기준: (" + userLat + ", " + userLon + "), 조회한 매장 개수: " + stores.size());
        return ResponseEntity.ok(stores);
    }
}
