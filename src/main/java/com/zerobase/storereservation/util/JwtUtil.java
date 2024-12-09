package com.zerobase.storereservation.util;

import com.zerobase.storereservation.exception.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import static com.zerobase.storereservation.exception.JwtException.JwtErrorType.EXPIRED_TOKEN;
import static com.zerobase.storereservation.exception.JwtException.JwtErrorType.INVALID_TOKEN;

/**
 * JwtUtil
 * JWT 토큰 생성 및 검증을 위한 유틸리티 클래스
 */
@Component
@Slf4j
public class JwtUtil {

    // JWT 서명에 사용할 SecretKey
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    // 토큰 만료 시간 (1일)
    private static final long EXPIRATION_TIME = 86400000; // 24시간 (밀리초)

    /**
     * JWT 토큰 생성
     *
     * @param username 토큰에 포함할 사용자 이름
     * @return 생성된 JWT 토큰
     */
    public String generateToken(String username) {
        log.info("[JWT UTIL] 토큰 생성 요청 - 사용자 이름: {}", username);

        String token = Jwts.builder()
                .setSubject(username)                               // 사용자 식별 정보
                .setIssuedAt(new Date())                            // 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // 만료 시간
                .signWith(SECRET_KEY)                               // 서명
                .compact();

        log.info("[JWT UTIL] 토큰 생성 완료");
        return token;
    }

    /**
     * JWT 토큰 검증
     *
     * @param token 검증할 JWT 토큰
     * @return 검증 성공 여부
     * @throws JwtException 검증 실패 시 Custom 예외 발생
     */
    public boolean validateToken(String token) throws JwtException {
        log.info("[JWT UTIL] 토큰 검증 요청");
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)  // 서명 키 설정
                    .build()
                    .parseClaimsJws(token);     // 토큰 파싱 및 검증
            log.info("[JWT UTIL] 토큰 검증 성공");
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("[JWT UTIL] 토큰 검증 실패 - 만료된 토큰");
            throw new JwtException(EXPIRED_TOKEN);
        } catch (Exception e) {
            log.error("[JWT UTIL] 토큰 검증 실패 - 유효하지 않은 토큰");
            throw new JwtException(INVALID_TOKEN);
        }
    }

    /**
     * JWT 토큰에서 사용자 이름 추출
     *
     * @param token 사용자 이름을 추출할 JWT 토큰
     * @return 토큰에 포함된 사용자 이름
     */
    public String extractUsername(String token) {
        log.info("[JWT UTIL] 토큰에서 사용자 이름 추출 요청");

        String username = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)                      // 서명 키 설정
                .build()
                .parseClaimsJws(token).getBody().getSubject();  // Subject 에서 사용자 이름 추출

        log.info("[JWT UTIL] 사용자 이름 추출 완료 - 사용자 이름: {}", username);
        return username;
    }

    public SecretKey getSecretKey() {
        return SECRET_KEY;
    }
}
