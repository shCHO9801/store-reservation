package com.zerobase.storereservation.controller.auth;

import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.repository.UserRepository;
import com.zerobase.storereservation.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로그인된 사용자 정보 조회 - 성공적으로 사용자 정보를 반환")
    void getMeSuccess() throws Exception {
        // given: 테스트를 위한 사용자 생성 및 JWT 토큰 생성
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(com.zerobase.storereservation.entity.constants.Role.CUSTOMER);
        userRepository.save(user);

        String token = jwtUtil.generateToken(username);

        // when & then: /api/auth/me 엔드포인트 호출 및 응답 검증
        mockMvc.perform(
                        get("/api/auth/me")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    @DisplayName("로그인된 사용자 정보 조회 - 인증 실패로 401 반환")
    void getMeUnauthorized() throws Exception {
        // when & then: 인증 없이 /api/auth/me 엔드포인트 호출 및 응답 검증
        mockMvc.perform(
                        get("/api/auth/me")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-001"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }
}