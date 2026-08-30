package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.request.SubjectRequest;
import com.exammatrix.backend.dto.response.SubjectResponse;

import java.util.List;

public interface SubjectService {
    List<SubjectResponse> getAllSubjects();

    SubjectResponse createNewSubject(SubjectRequest request);

    SubjectResponse getSubjectById(Integer id);

    SubjectResponse updateSubjectById(Integer id, SubjectRequest request);

    void deleteSubjectById(Integer id);
}