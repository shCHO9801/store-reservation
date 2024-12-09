package com.zerobase.storereservation.security;

import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * UserDetailsImpl
 * - Spring Security 의 UserDetails 인터페이스를 구현한 클래스
 * - 사용자 정보와 권한 정보를 관리
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    // 사용자 엔티티
    private final User user;

    // 사용자 권한 정보
    private final List<GrantedAuthority> authorities;

    // 로깅 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 생성자
     *
     * @param user 사용자 엔티티
     */
    public UserDetailsImpl(User user) {
        this.loggingUtil = new LoggingUtil();
        loggingUtil.logRequest("CREATE USER DETAILS", user.getUsername());
        this.user = user;
        this.authorities = List.of(() -> "ROLE_" + user.getRole().name());
        loggingUtil.logSuccess("CREATE USER DETAILS", "생성 완료: " + user.getUsername());
    }

    /**
     * 사용자 권한 정보 반환
     *
     * @return GrantedAuthority 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        loggingUtil.logRequest("GET AUTHORITIES", user.getUsername());
        return authorities;
    }

    /**
     * 사용자 비밀번호 반환
     *
     * @return 사용자 비밀번호
     */
    @Override
    public String getPassword() {
        loggingUtil.logRequest("GET PASSWORD", user.getUsername());
        return user.getPassword();
    }


    /**
     * 사용자 이름 반환
     *
     * @return 사용자 이름
     */
    @Override
    public String getUsername() {
        loggingUtil.logRequest("GET USERNAME", user.getUsername());
        return user.getUsername();
    }

    /**
     * 계정 만료 여부 반환
     *
     * @return true (계정이 만료되지 않음)
     */
    @Override
    public boolean isAccountNonExpired() {
        loggingUtil.logRequest("CHECK ACCOUNT NON-EXPIRED", user.getUsername());
        return true;
    }

    /**
     * 계정 잠금 여부 반환
     *
     * @return true (계정이 잠기지 않음)
     */
    @Override
    public boolean isAccountNonLocked() {
        loggingUtil.logRequest("CHECK ACCOUNT NON-LOCKED", user.getUsername());
        return true;
    }

    /**
     * 자격 증명 만료 여부 반환
     *
     * @return true (자격 증명이 만료되지 않음)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        loggingUtil.logRequest("CHECK CREDENTIALS NON-EXPIRED", user.getUsername());
        return true;
    }

    /**
     * 계정 활성화 여부 반환
     *
     * @return true (계정이 활성화됨)
     */
    @Override
    public boolean isEnabled() {
        loggingUtil.logRequest("CHECK ACCOUNT ENABLED", user.getUsername());
        return true;
    }
}
