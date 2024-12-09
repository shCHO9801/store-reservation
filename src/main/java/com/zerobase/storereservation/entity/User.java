package com.zerobase.storereservation.entity;

import com.zerobase.storereservation.entity.constants.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * User
 * 애플리케이션 사용자 정보를 저장하는 엔티티
 * - Spring Security 의 UserDetails 를 구현하여 인증 및 권한 관리를 지원
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "app_user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // 사용자 ID

    @Column(nullable = false, unique = true)
    private String username;    // 사용자 이름 (로그인 ID로 사용)

    @Column(nullable = false)
    private String password;    // 사용자 비밀번호 (암호화 저장)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;          // 사용자 역할 (CUSTOMER, PARTNER)

    /**
     * 사용자의 권한 목록을 반환
     * - Role 열거형에서 이름을 가져와 GrantedAuthority 로 반환
     *
     * @return GrantedAuthority 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role.name());
    }

    /**
     * 사용자 이름을 반환
     * - UserDetails 인터페이스 구현 (Spring Security 에서 사용)
     *
     * @return 사용자 이름
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 사용자 비밀번호를 반환
     * - UserDetails 인터페이스 구현
     *
     * @return 사용자 비밀번호
     */
    @Override
    public String getPassword() {
        return password;
    }
}
