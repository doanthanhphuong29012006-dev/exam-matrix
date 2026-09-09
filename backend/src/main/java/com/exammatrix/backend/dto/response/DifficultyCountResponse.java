package com.exammatrix.backend.dto.response;

import com.exammatrix.backend.entity.enums.Difficulty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DifficultyCountResponse {
    private Difficulty difficulty;
    private Long count;
}