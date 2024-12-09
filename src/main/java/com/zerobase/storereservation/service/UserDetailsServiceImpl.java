package com.zerobase.storereservation.service;

import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.exception.ErrorCode;
import com.zerobase.storereservation.repository.UserRepository;
import com.zerobase.storereservation.security.UserDetailsImpl;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsServiceImpl
 * - Spring Security 의 UserDetailsService 인터페이스 구현 클래스
 * - 사용자 인증 정보를 데이터베이스에서 조회하고 UserDetails 를 반환
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    // 사용자 관련 데이터 작업을 처리하는 Repository
    private final UserRepository userRepository;

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 사용자 이름으로 사용자 정보 조회
     *
     * @param username 사용자 이름
     * @return UserDetails 객체 (Spring Security 에서 인증 처리에 사용)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        loggingUtil.logRequest("LOAD USER BY USERNAME", username);

        User user = findUserByUsername(username);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        loggingUtil.logSuccess("LOAD USER BY USERNAME", "UserDetails 생성 완료: " + username);
        return userDetails;
    }

    // ==== Private Helper Methods ====
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    loggingUtil.logError("LOAD USER BY USERNAME", "사용자를 찾을 수 없음: " + username);
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });
    }
}
