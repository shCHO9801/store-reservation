package com.zerobase.storereservation.dto;

import com.zerobase.storereservation.entity.constants.Role;
import lombok.Builder;
import lombok.Data;

public class UserDto {

    @Data
    public static class CreateRequest {
        private String username;
        private String password;
        private Role role;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String username;
        private Role role;
    }
}
