package com.exammatrix.backend.dto.request;

import com.exammatrix.backend.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private UserStatus status;
}