package com.exammatrix.backend.service.impl;

import com.exammatrix.backend.dto.request.SubjectRequest;
import com.exammatrix.backend.dto.response.SubjectResponse;
import com.exammatrix.backend.entity.Subject;
import com.exammatrix.backend.repository.SubjectRepository;
import com.exammatrix.backend.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {
    private final SubjectRepository subjectRepository;

    @Override
    public List<SubjectResponse> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAll();

        return subjects.stream()
                .map(subject -> SubjectResponse.builder()
                        .id(subject.getId())
                        .code(subject.getCode())
                        .name(subject.getName())
                        .description(subject.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public SubjectResponse createNewSubject(SubjectRequest request) {
        Subject newSubject = Subject.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Subject savedSubject = subjectRepository.save(newSubject);

        return SubjectResponse.builder()
                .id(savedSubject.getId())
                .code(savedSubject.getCode())
                .name(savedSubject.getName())
                .description(savedSubject.getDescription())
                .build();
    }
}