package com.exammatrix.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerRequest {
    @NotBlank(message = "Nội dung đáp án không được để trống")
    private String content;

    @NotNull(message = "Phải xác định đáp án đúng hoặc sai")
    private Boolean isCorrect;
}