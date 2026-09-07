package com.exammatrix.backend.dto.response;

import com.exammatrix.backend.entity.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAvailabilityResponse {
    private Integer chapterId;
    private Difficulty difficulty;
    private Long available;
}