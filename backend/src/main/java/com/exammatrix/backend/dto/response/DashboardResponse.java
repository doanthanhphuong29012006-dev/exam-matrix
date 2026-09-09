package com.exammatrix.backend.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private Long totalSubjects;
    private Long totalChapters;
    private Long totalQuestions;
    private Long totalMatrices;
    private Long totalPapers;
    private List<DifficultyCountResponse> difficulty;
    private List<ExamMatrixResponse> recentMatrices;
}