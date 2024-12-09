package com.zerobase.storereservation.dto;

import com.zerobase.storereservation.entity.constants.Role;
import lombok.Builder;
import lombok.Data;

/**
 * UserDto
 * 사용자 관련 요청 및 응답 데이터를 처리하기 위한 DTO 클래스
 */
public class UserDto {

    /**
     * CreateRequest
     * 사용자 생성 요청 DTO
     * - 회원가입 시 필요한 데이터를 전달하는 클래스
     */
    @Data
    public static class CreateRequest {
        private String username;        // 사용자 이름 (고유)
        private String password;        // 사용자 비밀번호
        private Role role;              // 사용자 역할 (기본값 : CUSTOMER)
    }

    /**
     * Response
     * 사용자 응답 DTO
     * - 사용자 정보를 반환하기 위한 데이터 구조
     */
    @Data
    @Builder
    public static class Response {
        private Long id;                // 사용자 ID
        private String username;        // 사용자 이름
        private Role role;              // 사용자 역할
    }
}