package com.zerobase.storereservation.util;

import com.zerobase.storereservation.exception.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("JWT 토큰 생성 및 검증 성공")
    void shouldGenerateAndValidateTokenSuccessfully() throws JwtException {
        // given
        String username = "testUser";

        // when
        String token = jwtUtil.generateToken(username);

        // then
        assertTrue(jwtUtil.validateToken(token), "토큰 검증에 실패했습니다.");
        assertEquals(username, jwtUtil.extractUsername(token), "추출된 사용자 이름이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("JWT 토큰 만료 검증 실패")
    void shouldFailValidationForExpiredToken() {
        // given
        String expiredToken = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000)) // 1초 전 발행
                .setExpiration(new Date(System.currentTimeMillis() - 500)) // 0.5초 전 만료
                .signWith(jwtUtil.getSecretKey()) // JwtUtil의 시크릿 키 사용
                .compact();

        // then
        JwtException exception = assertThrows(JwtException.class, () -> jwtUtil.validateToken(expiredToken));
        assertEquals("토큰이 만료되었습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰 검증 실패")
    void shouldFailValidationForInvalidToken() {
        // given
        String invalidToken = "invalid.token.value";

        // then
        JwtException exception = assertThrows(JwtException.class, () -> jwtUtil.validateToken(invalidToken));
        assertEquals("유효하지 않은 토큰입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("JWT 빈 토큰 검증 실패")
    void shouldFailValidationForEmptyOrNullToken() {
        // then
        JwtException emptyException = assertThrows(JwtException.class, () -> jwtUtil.validateToken(""));
        assertEquals("유효하지 않은 토큰입니다.", emptyException.getMessage());

        JwtException nullException = assertThrows(JwtException.class, () -> jwtUtil.validateToken(null));
        assertEquals("유효하지 않은 토큰입니다.", nullException.getMessage());
    }
}
