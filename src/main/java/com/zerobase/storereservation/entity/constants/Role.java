package com.zerobase.storereservation.entity.constants;

public enum Role {
    CUSTOMER("일반 사용자"),
    PARTNER("파트너");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
