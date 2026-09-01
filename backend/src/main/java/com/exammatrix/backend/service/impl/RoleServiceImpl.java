package com.exammatrix.backend.service.impl;

import com.exammatrix.backend.dto.response.RoleResponse;
import com.exammatrix.backend.entity.Role;
import com.exammatrix.backend.repository.RoleRepository;
import com.exammatrix.backend.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleResponse> responses = new ArrayList<>();

        for (Role role : roles) {
            String roleName = role
                    .getName()
                    .toUpperCase(Locale.ROOT);

            RoleResponse response = new RoleResponse(role.getId(), roleName);
            responses.add(response);
        }

        return responses;
    }
}