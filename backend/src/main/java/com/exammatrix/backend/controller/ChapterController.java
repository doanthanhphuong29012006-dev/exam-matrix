package com.exammatrix.backend.controller;

import com.exammatrix.backend.dto.request.ChapterRequest;
import com.exammatrix.backend.dto.response.ChapterResponse;
import com.exammatrix.backend.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;

    @GetMapping
    public ResponseEntity<List<ChapterResponse>> getAllChapters(@RequestParam(required = false) Integer subjectId) {
        List<ChapterResponse> responses = chapterService.getAllChapters(subjectId);

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChapterResponse> getDetailChapter(@PathVariable("id") Integer id) {
        ChapterResponse response = chapterService.getDetailChapter(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<ChapterResponse> createChapter(@Valid @RequestBody ChapterRequest request) {
        ChapterResponse response = chapterService.createNewChapter(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ChapterResponse> updateChapter(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ChapterRequest request
    ) {
        ChapterResponse response = chapterService.updateChapterById(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChapter(@PathVariable("id") Integer id) {
        chapterService.deleteChapterById(id);

        return ResponseEntity.noContent().build();
    }
}