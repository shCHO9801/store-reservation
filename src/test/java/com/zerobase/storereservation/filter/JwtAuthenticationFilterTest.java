package com.zerobase.storereservation.filter;

import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.entity.constants.Role;
import com.zerobase.storereservation.repository.UserRepository;
import com.zerobase.storereservation.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test") // 테스트 프로파일 활성화
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    // Authorization 헤더 이름
    private static final String AUTH_HEADER = "Authorization";
    // Bearer 토큰 접두어
    private static final String BEARER_PREFIX = "Bearer ";

    @BeforeEach
    void setUp() {
        // 테스트 전에 모든 사용자 삭제
        userRepository.deleteAll();

        // "testUser" 생성 및 저장
        User user = User.builder()
                .username("testUser")
                .password(passwordEncoder.encode("password"))
                .role(Role.CUSTOMER)
                .build();
        userRepository.save(user);
    }


    @Test
    @DisplayName("JWT 필터 - 올바른 토큰으로 인증 요청")
    void testFilterWithValidToken() throws Exception {
        // given
        String username = "testUser";
        String token = jwtUtil.generateToken(username);
        User user = userRepository.findByUsername(username).orElseThrow();

        // when & then
        mockMvc.perform(get("/api/auth/me")
                        .header(AUTH_HEADER, BEARER_PREFIX + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        String.format("{\"id\":%d,\"username\":\"testUser\",\"role\":\"CUSTOMER\"}", user.getId())
                ));
    }

    @Test
    @DisplayName("JWT 필터 - 토큰 없이 인증 요청 실패")
    void testFilterWithoutToken() throws Exception {
        // when & then
        mockMvc.perform(get("/api/protected-endpoint")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
