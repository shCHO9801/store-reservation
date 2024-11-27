package com.zerobase.storereservation.dto;

import lombok.Builder;
import lombok.Data;

public class UserDto {

    @Data
    public static class CreateRequest {
        private String username;
        private String password;
        private String role;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String username;
        private String role;
    }
}
