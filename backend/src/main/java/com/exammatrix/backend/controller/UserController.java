package com.exammatrix.backend.controller;

import com.exammatrix.backend.dto.request.CreateUserRequest;
import com.exammatrix.backend.dto.request.UpdateUserRequest;
import com.exammatrix.backend.dto.request.UpdateUserStatusRequest;
import com.exammatrix.backend.dto.response.PageResponse;
import com.exammatrix.backend.dto.response.UserResponse;
import com.exammatrix.backend.enums.UserStatus;
import com.exammatrix.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>>
    getAllUsers(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            String role,

            @RequestParam(required = false)
            UserStatus status,

            @RequestParam(defaultValue = "0")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer size
    ) {
        PageResponse<UserResponse> response = userService.getAllUsers(search, role, status, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") UUID id) {
        UserResponse response = userService.getUserById(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUserById(id, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable("id") UUID id,

            @Valid
            @RequestBody
            UpdateUserStatusRequest request) {
        UserResponse response = userService.updateUserStatus(id, request);

        return ResponseEntity.ok(response);
    }
}