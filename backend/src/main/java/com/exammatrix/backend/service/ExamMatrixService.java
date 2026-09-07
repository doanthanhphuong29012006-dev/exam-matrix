package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.request.ExamMatrixRequest;
import com.exammatrix.backend.dto.response.ExamMatrixResponse;
import com.exammatrix.backend.dto.response.PageResponse;

import java.util.UUID;

public interface ExamMatrixService {
    PageResponse<ExamMatrixResponse> getAllExamMatrices(
            String search,
            Integer subjectId,
            Integer page,
            Integer size
    );

    ExamMatrixResponse getExamMatrixById(UUID id);

    ExamMatrixResponse createExamMatrix(ExamMatrixRequest request);

    ExamMatrixResponse updateExamMatrix(UUID id, ExamMatrixRequest request);

    void deleteExamMatrix(UUID id);
}