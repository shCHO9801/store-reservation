package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.UserDto;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.zerobase.storereservation.exception.ErrorCode.USER_ALREADY_EXISTS;
import static com.zerobase.storereservation.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto.Response createUser(UserDto.CreateRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(USER_ALREADY_EXISTS);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .role(request.getRole())
                .build();

        user = userRepository.save(user);
        return UserDto.Response.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public UserDto.Response getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return UserDto.Response.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
