package com.exammatrix.backend.dto.request;

import com.exammatrix.backend.entity.enums.Difficulty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrixConfigRequest {
    @NotNull(message = "Chương không được để trống")
    @Min(value = 1, message = "ID chương không hợp lệ")
    private Integer chapterId;

    @NotNull(message = "Độ khó không được để trống")
    private Difficulty difficulty;

    @NotNull(message = "Số lượng câu hỏi không được để trống")
    @Min(value = 0, message = "Số lượng câu hỏi không được âm")
    private Integer quantity;
}