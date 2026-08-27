package com.exammatrix.backend.controller;

import com.exammatrix.backend.dto.request.SubjectRequest;
import com.exammatrix.backend.dto.response.SubjectResponse;
import com.exammatrix.backend.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getAllSubjects() {
        List<SubjectResponse> responses = subjectService.getAllSubjects();

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(@Valid @RequestBody SubjectRequest request) {
        SubjectResponse response= subjectService.createNewSubject(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}