package com.exammatrix.backend.dto.response;

import com.exammatrix.backend.entity.enums.Difficulty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrixConfigResponse {
    private Integer chapterId;
    private Difficulty difficulty;
    private Integer quantity;
}