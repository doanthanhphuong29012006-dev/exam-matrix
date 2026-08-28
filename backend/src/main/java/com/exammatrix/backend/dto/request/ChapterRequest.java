package com.exammatrix.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterRequest {
    @NotBlank(message = "Tên chương không được để trống!")
    @Size(max = 50, message = "Tên chương không được vượt quá 50 ký tự!")
    private String name;

    @NotNull(message = "Số chương không được để trống!")
    @Min(value = 1, message = "Số chương không được nhỏ hơn 1!")
    private Integer orderIndex;

    @NotNull
    @Min(value = 1, message = "Id môn học không hợp lệ!")
    private Integer subjectId;

}