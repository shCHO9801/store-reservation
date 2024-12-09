package com.zerobase.storereservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProtectedEndpointController {
    @GetMapping("/protected-endpoint")
    public ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok("Access Granted");
    }
}
