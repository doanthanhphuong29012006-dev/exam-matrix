package com.exammatrix.backend.service.impl;

import com.exammatrix.backend.dto.request.SubjectRequest;
import com.exammatrix.backend.dto.response.SubjectResponse;
import com.exammatrix.backend.entity.Subject;
import com.exammatrix.backend.repository.SubjectRepository;
import com.exammatrix.backend.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {
    private final SubjectRepository subjectRepository;

    @Override
    public List<SubjectResponse> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAll();

        List<SubjectResponse> responses = new ArrayList<>();

        for (Subject subject : subjects) {
            SubjectResponse response = convertToResponse(subject);
            responses.add(response);
        }

        return responses;
    }

    @Override
    public SubjectResponse getSubjectById(Integer id) {
        Subject subject = findSubjectById(id);

        return convertToResponse(subject);
    }

    @Override
    public SubjectResponse createNewSubject(SubjectRequest request) {
        String code = request.getCode()
                .trim()
                .toUpperCase();

        boolean codeExisted = subjectRepository.existsByCodeIgnoreCase(code);

        if (codeExisted) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã môn học đã tồn tại"
            );
        }

        Subject subject = new Subject();

        subject.setCode(code);
        subject.setName(request.getName().trim());
        subject.setDescription(request.getDescription());

        Subject savedSubject = subjectRepository.save(subject);

        return convertToResponse(savedSubject);
    }

    @Override
    public SubjectResponse updateSubjectById(Integer id, SubjectRequest request) {
        Subject subject = findSubjectById(id);

        String code = request.getCode()
                .trim()
                .toUpperCase();
        boolean existCodeOnAnotherSubject = subjectRepository.existsByCodeIgnoreCaseAndIdNot(code, id);
        if (existCodeOnAnotherSubject) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã môn học đã được sử dụng"
            );
        }

        subject.setCode(code);
        subject.setName(request.getName().trim());
        subject.setDescription(request.getDescription());

        Subject savedSubject = subjectRepository.save(subject);

        return convertToResponse(savedSubject);
    }

    @Override
    public void deleteSubjectById(Integer id) {
        Subject subject = findSubjectById(id);

        subjectRepository.delete(subject);
    }

    private Subject findSubjectById(Integer id) {
        Optional<Subject> result = subjectRepository.findById(id);

        if (result.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy môn học có id " + id
            );
        }

        return result.get();
    }

    private SubjectResponse convertToResponse(Subject subject) {
        SubjectResponse response = new SubjectResponse();

        response.setId(subject.getId());
        response.setCode(subject.getCode());
        response.setName(subject.getName());
        response.setDescription(subject.getDescription());

        return response;
    }
}