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
    @DisplayName("JWT 토큰 생성 및 검증")
    void testGenerateAndValidateToken() throws JwtException {
        //given
        String username = "testUser";

        //when
        String token = jwtUtil.generateToken(username);

        //then
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    @DisplayName("JWT 토큰 만료 검증")
    void testExpiredToken() {
        //given
        String expiredToken = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000))
                .setExpiration(new Date(System.currentTimeMillis() - 500))
                .signWith(jwtUtil.getSecretKey()) // JwtUtil의 키 사용
                .compact();

        //then
        assertThrows(JwtException.class, () -> jwtUtil.validateToken(expiredToken));
    }


    @Test
    @DisplayName("JWT 유효하지 않은 토큰 검증")
    void testInvalidToken() {
        //given
        String invalidToken = "invalid.token.value";

        //then
        assertThrows(JwtException.class, () -> jwtUtil.validateToken(invalidToken));
    }

    @Test
    @DisplayName("JWT 빈 토큰 검증")
    void testEmptyToken() {
        //then
        assertThrows(JwtException.class, () -> jwtUtil.validateToken(""));
        assertThrows(JwtException.class, () -> jwtUtil.validateToken(null));
    }
}