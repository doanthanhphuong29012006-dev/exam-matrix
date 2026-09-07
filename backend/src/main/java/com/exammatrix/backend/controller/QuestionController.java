package com.exammatrix.backend.controller;

import com.exammatrix.backend.dto.request.QuestionRequest;
import com.exammatrix.backend.dto.response.PageResponse;
import com.exammatrix.backend.dto.response.QuestionAvailabilityResponse;
import com.exammatrix.backend.dto.response.QuestionResponse;
import com.exammatrix.backend.entity.enums.Difficulty;
import com.exammatrix.backend.entity.enums.QuestionType;
import com.exammatrix.backend.service.QuestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Validated
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/availability")
    public ResponseEntity<List<QuestionAvailabilityResponse>>
    getAvailability(
            @RequestParam
            @Min(value = 1, message = "ID môn học không hợp lệ")
            Integer subjectId
    ) {
        return ResponseEntity.ok(
                questionService.getAvailability(subjectId)
        );
    }

    @GetMapping
    public ResponseEntity<PageResponse<QuestionResponse>>
    getAllQuestions(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Integer subjectId,

            @RequestParam(required = false)
            Integer chapterId,

            @RequestParam(required = false)
            Difficulty difficulty,

            @RequestParam(required = false)
            QuestionType type,

            @RequestParam(defaultValue = "0")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer size
    ) {
        PageResponse<QuestionResponse> response =
                questionService.getAllQuestions(
                        search,
                        subjectId,
                        chapterId,
                        difficulty,
                        type,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                questionService.getQuestionById(id)
        );
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(
            @Valid @RequestBody QuestionRequest request
    ) {
        QuestionResponse response =
                questionService.createQuestion(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable UUID id,
            @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(
                questionService.updateQuestion(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable UUID id
    ) {
        questionService.deleteQuestion(id);

        return ResponseEntity.noContent().build();
    }
}