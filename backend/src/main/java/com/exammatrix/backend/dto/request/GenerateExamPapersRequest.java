package com.exammatrix.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateExamPapersRequest {
    @NotNull(message = "Số mã đề không được để trống")
    @Min(value = 1, message = "Phải sinh ít nhất 1 mã đề")
    @Max(value = 20, message = "Chỉ được sinh tối đa 20 mã đề một lần")
    private Integer numberOfPapers;
}