package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.request.CreateUserRequest;
import com.exammatrix.backend.dto.request.UpdateUserRequest;
import com.exammatrix.backend.dto.request.UpdateUserStatusRequest;
import com.exammatrix.backend.dto.response.PageResponse;
import com.exammatrix.backend.dto.response.UserResponse;
import com.exammatrix.backend.enums.UserStatus;

import java.util.UUID;

public interface UserService {
    PageResponse<UserResponse> getAllUsers(
            String search,
            String role,
            UserStatus status,
            Integer page,
            Integer size
    );

    UserResponse getUserById(UUID id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUserById(UUID id, UpdateUserRequest request);

    UserResponse updateUserStatus(UUID id, UpdateUserStatusRequest request);
}