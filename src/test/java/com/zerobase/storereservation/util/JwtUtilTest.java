package com.zerobase.storereservation.util;

import com.zerobase.storereservation.exception.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("JWT 토큰 생성 및 검증")
    void testGenerateAndValidateToken() {
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
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS512))
                .compact();

        //then
        assertThrows(JwtException.class, () -> jwtUtil.validateToken(expiredToken));
    }
}