package com.exammatrix.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectRequest {
    @NotBlank(message = "Mã môn học không được để trống")
    @Size(max = 10, message = "Mã môn học không được vượt quá 10 ký tự!")
    private String code;

    @NotBlank(message = "Tên môn học không được để trống")
    @Size(max = 50, message = "Tên môn học không được vượt quá 50 ký tự!")
    private String name;

    private String description;
}