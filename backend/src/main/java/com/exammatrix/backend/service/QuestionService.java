package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.request.QuestionRequest;
import com.exammatrix.backend.dto.response.PageResponse;
import com.exammatrix.backend.dto.response.QuestionAvailabilityResponse;
import com.exammatrix.backend.dto.response.QuestionResponse;
import com.exammatrix.backend.entity.enums.Difficulty;
import com.exammatrix.backend.entity.enums.QuestionType;

import java.util.List;
import java.util.UUID;

public interface QuestionService {
    PageResponse<QuestionResponse> getAllQuestions(
        String search,
        Integer subjectId,
        Integer chapterId,
        Difficulty difficulty,
        QuestionType type,
        Integer page,
        Integer size
    );

    QuestionResponse getQuestionById(UUID id);

    QuestionResponse createQuestion(QuestionRequest request);

    QuestionResponse updateQuestion(
            UUID id,
            QuestionRequest request
    );

    void deleteQuestion(UUID id);

    List<QuestionAvailabilityResponse> getAvailability(
            Integer subjectId
    );
}