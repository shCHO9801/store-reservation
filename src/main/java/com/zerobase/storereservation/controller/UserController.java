package com.zerobase.storereservation.controller;

import com.zerobase.storereservation.dto.UserDto;
import com.zerobase.storereservation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto.Response> createUser(
            @RequestBody UserDto.CreateRequest request
    ) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto.Response> getUser(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
