package com.exammatrix.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamPaperDetailResponse {
    private UUID id;
    private UUID matrixId;
    private String examCode;
    private LocalDateTime createdAt;
    private List<PaperQuestionResponse> questions;
}