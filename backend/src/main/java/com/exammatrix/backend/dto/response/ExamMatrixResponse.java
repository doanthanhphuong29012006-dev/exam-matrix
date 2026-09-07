package com.exammatrix.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamMatrixResponse {
    private UUID id;
    private UUID teacherId;
    private Integer subjectId;
    private String title;
    private Integer duration;
    private Integer totalQuestions;
    private List<MatrixConfigResponse> configs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}