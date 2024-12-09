package com.zerobase.storereservation.config;

import com.zerobase.storereservation.exception.CustomAuthenticationEntryPoint;
import com.zerobase.storereservation.filter.JwtAuthenticationFilter;
import com.zerobase.storereservation.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * SecurityConfig
 * Spring Security 설정 클래스
 * - 인증 및 권한 관련 설정
 * - JWT 필터 적용
 * - CORS 및 CSRF 설정
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    // 사용자 정보 서비스
    private final UserDetailsServiceImpl userDetailsService;

    //인증 실패 시 처리 로직
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    // JWT 인증 필터
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 공용 URL 목록 (인증 불필요한 경로)
    public static final String[] PUBLIC_URLS = {
            "/api/auth/signup",
            "/api/auth/login",
            "/api/users"
    };

    /**
     * SecurityFilterChain 설정
     * - 각 요청 URL 에 대한 인증 및 권한 설정
     * - JWT 필터 적용
     * - Stateless 세션 정책 적용
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 예외 발생 시 처리
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("SecurityFilterChain 설정을 시작합니다.");

        http.csrf(csrf -> {
                    log.info("CSRF 보호 비활성화 설정");
                    csrf.disable();
                })   // CSRF 보호 비활성화 (JWT 사용)
                .cors(cors -> {
                    log.info("CORS 설정 적용");
                    cors.configurationSource(corsConfigurationSource());
                }) // CORS 설정
                .authorizeHttpRequests(auth -> {
                            log.info("인증 및 권한 설정 중...");
                            // 인증 없이 접근 가능한 URL
                            auth.requestMatchers(PUBLIC_URLS).permitAll()
                                    // 테스트용 엔드포인트
                                    .requestMatchers("/api/protected-endpoint").authenticated()
                                    // 인증이 필요한 URL
                                    .requestMatchers("/api/auth/me").authenticated()
                                    // PARTNER 권한 필요
                                    .requestMatchers("/stores/**").hasRole("PARTNER")
                                    // 기본적으로 모든 요청은 인증 필요
                                    .anyRequest().authenticated();
                        }
                )
                // 인증 실패 처리
                .exceptionHandling(ex -> {
                    log.info("인증 실패 로직 설정");
                    ex.authenticationEntryPoint(customAuthenticationEntryPoint);
                })
                // Stateless 세션
                .sessionManagement(session -> {
                    log.info("Stateless 세션 정책 적용");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                // 인증 제공자 설정
                .authenticationProvider(authenticationProvider())
                // JWT 필터 설정
                .addFilterBefore(
                        jwtAuthenticationFilter, // 빈으로 주입받은 필터 사용
                        UsernamePasswordAuthenticationFilter.class
                );
        log.info("SecurityFilterChain 설정이 완료되었습니다.");
        return http.build();
    }

    /**
     * CORS 설정
     * - 모든 도메인, 헤더, 메서드 허용
     * - 크레덴셜 허용
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("CORS 설정을 시작합니다.");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // 모든 도메인 허용
        configuration.addAllowedHeader("*");        // 모든 헤더 허용
        configuration.addAllowedMethod("*");        // 모든 HTTP 메서드 허용
        configuration.setAllowCredentials(true);    // 인증 정보 포함 허용

        log.info("CORS 설정이 완료 되었습니다.");

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * DaoAuthenticationProvider 설정
     * - UserDetailsService 와 PasswordEncoder 를 사용
     *
     * @return DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        log.info("DaoAuthenticationProvider 설정을 시작합니다.");

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // 사용자 정보 처리
        provider.setPasswordEncoder(passwordEncoder());     // 비밀번호 인코딩

        log.info("DaoAuthenticationProvider 설정이 완료 되었습니다.");
        return provider;
    }

    /**
     * PasswordEncoder 설정
     * - BCryptPasswordEncoder 사용
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("PasswordEncoder 설정을 시작합니다.");
        log.info("BCryptPasswordEncoder 를 사용합니다.");
        return new BCryptPasswordEncoder(); // 기본 강도 10
    }
}