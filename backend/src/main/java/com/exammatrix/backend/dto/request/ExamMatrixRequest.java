package com.exammatrix.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamMatrixRequest {
    @NotNull(message = "Môn học không được để trống")
    @Min(value = 1, message = "ID môn học không hợp lệ")
    private Integer subjectId;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 100, message = "Tiêu đề không được vượt quá 100 ký tự")
    private String title;

    @NotNull(message = "Thời gian làm bài không được để trống")
    @Min(value = 1, message = "Thời gian làm bài phải lớn hơn 0")
    private Integer duration;

    @NotNull(message = "Tổng số câu không được để trống")
    @Min(value = 1, message = "Tổng số câu phải lớn hơn 0")
    private Integer totalQuestions;

    @NotEmpty(message = "Ma trận phải có ít nhất một cấu hình")
    @Valid
    private List<MatrixConfigRequest> configs;
}