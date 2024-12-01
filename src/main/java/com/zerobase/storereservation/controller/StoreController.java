package com.zerobase.storereservation.controller;

import com.zerobase.storereservation.dto.StoreDto;
import com.zerobase.storereservation.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreDto.Response> createStore(
            @RequestBody StoreDto.CreateRequest request
    ) {
        return ResponseEntity.ok(storeService.createStore(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDto.Response> getStoreById(
            @PathVariable Long id) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDto.Response> updateStore(
            @PathVariable Long id,
            @RequestBody StoreDto.CreateRequest request
    ) {
        return ResponseEntity.ok(storeService.updateStore(id, request));
    }

    @GetMapping
    public ResponseEntity<List<StoreDto.Response>> getStores(
            @RequestParam(required = false, defaultValue = "name")
            String sortBy,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon
    ) {
        List<StoreDto.Response> stores =
                storeService.getStores(sortBy, userLat, userLon);
        return ResponseEntity.ok(stores);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(
            @PathVariable Long id
    ) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }


}
