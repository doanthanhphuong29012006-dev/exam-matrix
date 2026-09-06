package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.request.ChangePasswordRequest;
import com.exammatrix.backend.dto.request.LoginRequest;
import com.exammatrix.backend.dto.request.UpdateProfileRequest;
import com.exammatrix.backend.dto.response.AuthResponse;
import com.exammatrix.backend.dto.response.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser();

    UserResponse updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    void logout();
}