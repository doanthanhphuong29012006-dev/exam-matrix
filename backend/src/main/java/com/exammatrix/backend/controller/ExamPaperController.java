package com.exammatrix.backend.controller;

import com.exammatrix.backend.dto.response.ExamPaperDetailResponse;
import com.exammatrix.backend.dto.response.PageResponse;
import com.exammatrix.backend.service.ExamPaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/exam-papers")
@RequiredArgsConstructor
public class ExamPaperController {
    private final ExamPaperService examPaperService;

    @GetMapping
    public ResponseEntity<PageResponse<ExamPaperDetailResponse>> getAllExamPapers(
            @RequestParam(required = false)
            UUID matrixId,

            @RequestParam(defaultValue = "0")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer size
    ) {
        return ResponseEntity.ok(examPaperService.getAllExamPapers(
                    matrixId,
                    page,
                    size
            )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamPaperDetailResponse> getExamPaperById(@PathVariable UUID id) {
        return ResponseEntity.ok(examPaperService.getExamPaperById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExamPaper(@PathVariable UUID id) {
        examPaperService.deleteExamPaper(id);

        return ResponseEntity.noContent().build();
    }
}