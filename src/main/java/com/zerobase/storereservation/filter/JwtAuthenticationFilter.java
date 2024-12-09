package com.zerobase.storereservation.filter;

import com.zerobase.storereservation.exception.JwtException;
import com.zerobase.storereservation.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter
 * JWT 기반 인증을 위한 필터
 * - 요청의 Authorization 헤더에서 JWT 를 추출 및 검증
 * - 인증 성공 시 SecurityContext 에 인증 저이보 저장
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Authorization 헤더 이름
    private static final String AUTH_HEADER = "Authorization";
    // Bearer 토큰 접두어
    private static final String BEARER_PREFIX = "Bearer ";
    // 토큰 검증 실패 메시지
    private static final String INVALID_TOKEN_MSG = "Invalid JWT Token";

    // JWT 유틸리티 클래스
    private final JwtUtil jwtUtil;
    // 사용자 상세 정보 서비스
    private final UserDetailsService userDetailsService;

    /**
     * 요청 필터링
     * - Authorization 헤더에서 JWT 를 추출 및 검증
     * - 유일한 토큰일 경우 SecurityContext 에 인증 정보 설정
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param chain    FilterChain
     * @throws IOException, ServletException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws IOException, ServletException {
        String header = request.getHeader(AUTH_HEADER);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            log.info("[JWT FILTER] Authorization 헤더 감지");

            String token = header.substring(BEARER_PREFIX.length()); // "Bearer " 제거
            log.info("[JWT FILTER] 토큰 추출");
            try {
                // 토큰 검증
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);   // 토큰에서 사용
                    log.info("[JWT FILTER] 토큰 유효성 검증 성공 - 사용자 이름: {}", username);

                    // 사용자 상세 정보 로드
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    log.info("[JWT FILTER] 사용자 정보 로드 성공 - 사용자 이름: {}", userDetails.getUsername());

                    // 인증 객체 생성 및 SecurityContext 설정
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("[JWT FILTER] SecurityContext 인증 정보 설정 완료");
                }
            } catch (JwtException e) {
                log.error("[JWT FILTER] 토큰 검증 실패 - 에러 메시지: {}", e.getMessage());
                SecurityContextHolder.clearContext();   // 인증 정보 제거
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, INVALID_TOKEN_MSG);
                return; // 필터 체인 중단
            }
        } else {
            log.info("[JWT FILTER] Authorization 헤더 없음 또는 Barer 토큰 아님");
        }
        chain.doFilter(request, response);  // 다음 필터로 요청 전달
    }
}