package com.exammatrix.backend.controller;

import com.exammatrix.backend.dto.request.ChapterRequest;
import com.exammatrix.backend.dto.response.ChapterResponse;
import com.exammatrix.backend.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;

    @PostMapping
    public ResponseEntity<ChapterResponse> createChapter(@Valid @RequestBody ChapterRequest request) {
        ChapterResponse response = chapterService.createNewChapter(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}