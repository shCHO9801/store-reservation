package com.zerobase.storereservation.controller;

import com.zerobase.storereservation.dto.UserDto;
import com.zerobase.storereservation.service.UserService;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserController
 * 사용자 관리 관련 API 를 제공하는 컨트롤러
 * - 사용자 생성 및 조회 기능 포함
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    // 사용자 관련 비지니스 로직을 처리하는 서비스
    private final UserService userService;

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 사용자 생성
     * - 새로운 사용자를 등록
     *
     * @param request 사용자 생성 요청 DTO
     * @return 생성된 사용자 정보
     */
    @PostMapping
    public ResponseEntity<UserDto.Response> createUser(
            @RequestBody UserDto.CreateRequest request
    ) {
        loggingUtil.logRequest("CREATE USER", request);

        // 사용자 생성 서비스 호출
        UserDto.Response response = userService.createUser(request);

        loggingUtil.logSuccess("CREATE USER", response);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 조회
     * - 특정 ID에 해당하는 사용자를 조회
     *
     * @param id 사용자 ID
     * @return 사용자 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.Response> getUser(
            @PathVariable Long id
    ) {
        loggingUtil.logRequest("GET USER", id);
        UserDto.Response response = userService.getUserById(id);
        loggingUtil.logSuccess("GET USER", response);
        return ResponseEntity.ok(response);
    }
}
