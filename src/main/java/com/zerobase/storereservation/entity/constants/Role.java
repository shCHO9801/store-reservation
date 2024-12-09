package com.zerobase.storereservation.entity.constants;

/**
 * Role
 * 사용자 역할을 정의하는 열거형
 * - CUSTOMER: 일반 사용자
 * - PARTNER: 파트너 (매장 관리자 등)
 */
public enum Role {
    CUSTOMER("일반 사용자"),
    PARTNER("파트너");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}