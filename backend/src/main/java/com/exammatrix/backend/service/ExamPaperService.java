package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.request.GenerateExamPapersRequest;
import com.exammatrix.backend.dto.response.ExamPaperDetailResponse;
import com.exammatrix.backend.dto.response.PageResponse;

import java.util.List;
import java.util.UUID;

public interface ExamPaperService {

    PageResponse<ExamPaperDetailResponse> getAllExamPapers(
            UUID matrixId,
            Integer page,
            Integer size
    );

    ExamPaperDetailResponse getExamPaperById(UUID id);

    List<ExamPaperDetailResponse> generateExamPapers(UUID matrixId, GenerateExamPapersRequest request);

    void deleteExamPaper(UUID id);
}