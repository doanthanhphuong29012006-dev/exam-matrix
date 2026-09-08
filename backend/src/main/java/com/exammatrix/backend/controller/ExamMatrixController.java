package com.exammatrix.backend.controller;

import com.exammatrix.backend.dto.request.ExamMatrixRequest;
import com.exammatrix.backend.dto.request.GenerateExamPapersRequest;
import com.exammatrix.backend.dto.response.ExamMatrixResponse;
import com.exammatrix.backend.dto.response.ExamPaperDetailResponse;
import com.exammatrix.backend.dto.response.PageResponse;
import com.exammatrix.backend.service.ExamMatrixService;
import com.exammatrix.backend.service.ExamPaperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/exam-matrices")
@RequiredArgsConstructor
public class ExamMatrixController {
    private final ExamMatrixService examMatrixService;
    private final ExamPaperService examPaperService;

    @GetMapping
    public ResponseEntity<PageResponse<ExamMatrixResponse>> getAllExamMatrices(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Integer subjectId,

            @RequestParam(defaultValue = "0")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer size
    ) {
        PageResponse<ExamMatrixResponse> response = examMatrixService.getAllExamMatrices(
                search,
                subjectId,
                page,
                size
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamMatrixResponse> getExamMatrixById(@PathVariable UUID id) {
        return ResponseEntity.ok(examMatrixService.getExamMatrixById(id));
    }

    @PostMapping
    public ResponseEntity<ExamMatrixResponse> createExamMatrix(@Valid @RequestBody ExamMatrixRequest request) {
        ExamMatrixResponse response = examMatrixService.createExamMatrix(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ExamMatrixResponse> updateExamMatrix(
            @PathVariable UUID id,

            @Valid
            @RequestBody
            ExamMatrixRequest request
    ) {
        return ResponseEntity.ok(examMatrixService.updateExamMatrix(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExamMatrix(@PathVariable UUID id) {
        examMatrixService.deleteExamMatrix(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/generate")
    public ResponseEntity<List<ExamPaperDetailResponse>> generateExamPapers(
            @PathVariable UUID id,
            @Valid @RequestBody
            GenerateExamPapersRequest request
    ) {
        List<ExamPaperDetailResponse> responses = examPaperService.generateExamPapers(
                id,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
}