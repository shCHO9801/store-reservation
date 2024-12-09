package com.zerobase.storereservation.controller.auth;

import com.zerobase.storereservation.dto.UserDto;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.UserRepository;
import com.zerobase.storereservation.util.JwtUtil;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.zerobase.storereservation.entity.constants.Role.CUSTOMER;
import static com.zerobase.storereservation.exception.ErrorCode.*;

/**
 * AuthenticationController
 * 인증 및 권한 관리를 위한 컨트롤러
 * - 회원가입, 로그인, 사용자 정보 조회 기능 제공
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    // 사용자 데이터를 관리하는 UserRepository
    private final UserRepository userRepository;

    // 비밀번호 암호화를 위한 PasswordEncoder
    private final PasswordEncoder passwordEncoder;

    // JWT 토큰 생성을 위한 유틸 클래스
    private final JwtUtil jwtUtil;

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 회원가입 메서드
     * - 새로운 사용자 등록
     *
     * @param request 회원가입 요청 DTO
     * @return 성공 또는 실패 메시지
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserDto.CreateRequest request) {
        loggingUtil.logRequest("SIGNUP", request.getUsername());

        // 사용자 이름 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            loggingUtil.logError(
                    "SIGNUP",
                    "이미 존재하는 사용자 이름 : " + request.getUsername());
            throw new CustomException(USER_ALREADY_EXISTS);
        }

        // User 객체 생성
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : CUSTOMER)
                .build();

        // 사용자 저장
        userRepository.save(user);
        loggingUtil.logSuccess("SIGNUP", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body("User Registered Successfully");
    }

    /**
     * 로그인 메서드
     * - 사용자 인증 후 JWT 토큰 반환
     *
     * @param request 로그인 요청 DTO
     * @return JWT 토큰 또는 에러 메시지
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto.CreateRequest request) {
        loggingUtil.logRequest("LOGIN", request.getUsername());

        // 사용자 존재 여부 확인
        User existingUser = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    loggingUtil.logError(
                            "LOGIN",
                            "존재하지 않는 사용자 : " + request.getUsername());
                    return new CustomException(USER_NOT_FOUND);
                });

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), existingUser.getPassword())) {
            loggingUtil.logError(
                    "LOGIN",
                    "잘못된 비밀번호"
            );
            throw new CustomException(INCORRECT_PASSWORD);
        }

        // JWT 토큰 생성 및 반환
        String token = jwtUtil.generateToken(existingUser.getUsername());
        loggingUtil.logSuccess(
                "LOGIN",
                "로그인 성공 : " + existingUser.getUsername()
                        + ", 토큰 발급 완료");
        return ResponseEntity.ok().body(Map.of("token", "Bearer " + token));
    }

    /**
     * 현재 인증된 사용자 정보 조회
     * - JWT 를 통해 인증된 사용자 정보 반환
     *
     * @return 사용자 정보
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto.Response> getMe() {
        loggingUtil.logRequest("USER INFO", "현재 사용자 정보 요청");

        // 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username;
        Object principal = authentication.getPrincipal();

        // principal 이 UserDetails 로 구현된 경우 처리
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        loggingUtil.logSuccess(
                "USER INFO",
                "현재 인증된 사용자 : " + username);

        // 사용자 정보 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    loggingUtil.logError(
                            "USER INFO",
                            "사용자 정보 조회 실패 : " + username);
                    return new CustomException(USER_NOT_FOUND);
                });

        loggingUtil.logSuccess("USER INFO", "사용자 조회 성공" + user.getUsername());

        // 사용자 정보를 DTO 로 반환
        return ResponseEntity.ok(
                UserDto.Response.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .build()
        );
    }
}