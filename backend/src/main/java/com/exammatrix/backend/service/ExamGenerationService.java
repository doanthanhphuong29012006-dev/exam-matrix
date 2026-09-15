package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.request.GenerateExamPapersRequest;
import com.exammatrix.backend.dto.response.ExamPaperDetailResponse;

import java.util.List;
import java.util.UUID;

public interface ExamGenerationService {
    List<ExamPaperDetailResponse> generateExamPapers(
            UUID matrixId,
            GenerateExamPapersRequest request
    );
}
