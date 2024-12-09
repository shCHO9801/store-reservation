package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository
 * - 사용자 데이터를 처리하기 위한 JPA Repository
 * - 사용자 관련 기본 및 커스텀 쿼리를 정의
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 사용자 이름으로 존재 여부 확인
     * - 회원가입 시 중복된 사용자 이름을 체크하기 위해 사용
     *
     * @param username 사용자 이름
     * @return 존재 여부 (true: 존재함, false: 존재하지 않음)
     */

    boolean existsByUsername(String username);

    /**
     * 사용자 이름으로 사용자 정보 조회
     * - 인증 및 권한 확인 시 사용
     *
     * @param username 사용자 이름
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByUsername(String username);
}
