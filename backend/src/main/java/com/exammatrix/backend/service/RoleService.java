package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> getAllRoles();
}