package com.zerobase.storereservation.service;

import com.zerobase.storereservation.dto.UserDto;
import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.exception.CustomException;
import com.zerobase.storereservation.repository.UserRepository;
import com.zerobase.storereservation.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.zerobase.storereservation.exception.ErrorCode.USER_ALREADY_EXISTS;
import static com.zerobase.storereservation.exception.ErrorCode.USER_NOT_FOUND;

/**
 * UserService
 * - 사용자 관리 비즈니스 로직을 처리
 * - 사용자 생성 및 조회 기능 제공
 */
@Service
@RequiredArgsConstructor
public class UserService {

    // 사용자 관련 데이터 작업을 처리하는 Repository
    private final UserRepository userRepository;

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil;

    /**
     * 사용자 생성
     * - 사용자 이름의 중복 여부를 확인한 후 사용자 생성
     *
     * @param request 사용자 생성 요청 DTO
     * @return 생성된 사용자 정보 DTO
     */
    public UserDto.Response createUser(UserDto.CreateRequest request) {
        loggingUtil.logRequest("CREATE USER", request);

        validateUsernameUniqueness(request.getUsername());

        User user = saveNewUser(request);
        UserDto.Response response = convertToDto(user);

        loggingUtil.logSuccess("CREATE USER", response);
        return response;
    }

    /**
     * 사용자 ID로 사용자 정보 조회
     *
     * @param id 사용자 ID
     * @return 사용자 정보 DTO
     */
    public UserDto.Response getUserById(Long id) {
        loggingUtil.logRequest("GET USER BY ID", id);

        User user = findUserById(id);
        UserDto.Response response = convertToDto(user);

        loggingUtil.logSuccess("GET USER BY ID", response);
        return response;
    }

    // ==== Private Helper Methods ====

    /**
     * 사용자 이름의 중복 여부 확인
     * - 사용자 이름이 이미 존재하는 경우 예외를 던짐
     *
     * @param username 사용자 이름
     * @throws CustomException 사용자 이름이 중복된 경우
     */
    private void validateUsernameUniqueness(String username) {
        if (userRepository.existsByUsername(username)) {
            loggingUtil.logError("CREATE USER", "중복된 사용자 이름: " + username);
            throw new CustomException(USER_ALREADY_EXISTS);
        }
    }

    /**
     * 새로운 사용자 생성 및 저장
     *
     * @param request 사용자 생성 요청 DTO
     * @return 생성된 사용자 엔티티
     */
    private User saveNewUser(UserDto.CreateRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .role(request.getRole())
                .build();

        return userRepository.save(user);
    }

    /**
     * 사용자 ID로 사용자 조회
     *
     * @param id 사용자 ID
     * @return 조회된 사용자 엔티티
     * @throws CustomException 사용자 정보를 찾을 수 없는 경우
     */
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    loggingUtil.logError("GET USER BY ID", "사용자 ID를 찾을 수 없음: " + id);
                    return new CustomException(USER_NOT_FOUND);
                });
    }

    /**
     * 사용자 엔티티를 Response DTO 로 변환
     *
     * @param user 사용자 엔티티
     * @return 변환된 사용자 Response DTO
     */
    private UserDto.Response convertToDto(User user) {
        return UserDto.Response.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
