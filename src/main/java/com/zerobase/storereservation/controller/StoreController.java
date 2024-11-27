package com.zerobase.storereservation.controller;

import com.zerobase.storereservation.dto.StoreDto;
import com.zerobase.storereservation.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
