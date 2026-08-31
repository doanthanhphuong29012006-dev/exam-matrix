package com.exammatrix.backend.service.impl;

import com.exammatrix.backend.dto.request.ChapterRequest;
import com.exammatrix.backend.dto.response.ChapterResponse;
import com.exammatrix.backend.entity.Chapter;
import com.exammatrix.backend.entity.Subject;
import com.exammatrix.backend.repository.ChapterRepository;
import com.exammatrix.backend.repository.SubjectRepository;
import com.exammatrix.backend.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;

    @Override
    public List<ChapterResponse> getAllChapters(Integer subjectId) {
        Sort sort = Sort.by(
                Sort.Order.asc("subject.id"),
                Sort.Order.asc("orderIndex")
        );
        List<ChapterResponse> responses = new ArrayList<>();
        List<Chapter> chapters;
        if (subjectId == null) {
            chapters = chapterRepository.findAll(sort);
        } else {
            chapters = chapterRepository.findAllBySubjectIdOrderByOrderIndexAsc(subjectId);
        }

        for (Chapter chapter : chapters) {
            ChapterResponse response = convertToResponse(chapter);
            responses.add(response);
        }

        return responses;
    }

    @Override
    public ChapterResponse getDetailChapter(Integer id) {
        Chapter chapter = findChapterById(id);

        return convertToResponse(chapter);
    }

    @Override
    public ChapterResponse createNewChapter(ChapterRequest request) {
        Chapter chapter = new Chapter();

        Subject subject = findSubjectById(request.getSubjectId());

        boolean existChapterInSubject = chapterRepository.existsBySubjectIdAndOrderIndex(
                request.getSubjectId(),
                request.getOrderIndex()
        );

        if (existChapterInSubject) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Chương này đã tồn tại trong môn học!"
            );
        }

        chapter.setName(request.getName().trim());
        chapter.setOrderIndex(request.getOrderIndex());
        chapter.setSubject(subject);

        Chapter savedChapter = chapterRepository.save(chapter);

        return convertToResponse(savedChapter);
    }

    @Override
    public ChapterResponse updateChapterById(Integer id, ChapterRequest request) {
        Chapter chapter = findChapterById(id);

        Subject subject = findSubjectById(request.getSubjectId());

        boolean orderIndexExisted = chapterRepository.existsBySubjectIdAndOrderIndexAndIdNot(
                request.getSubjectId(),
                request.getOrderIndex(),
                id
        );

        if (orderIndexExisted) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Chương đã tồn tại trong môn học khác"
            );
        }

        chapter.setName(request.getName().trim());
        chapter.setOrderIndex(request.getOrderIndex());
        chapter.setSubject(subject);

        Chapter savedChapter = chapterRepository.save(chapter);

        return convertToResponse(savedChapter);
    }

    @Override
    public void deleteChapterById(Integer id) {
        Chapter chapter = findChapterById(id);

        chapterRepository.delete(chapter);
    }

    private Chapter findChapterById(Integer id) {
        Optional<Chapter> result = chapterRepository.findById(id);

        if (result.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy chương có id: " + id
            );
        }

        return result.get();
    }

    private Subject findSubjectById(Integer id) {
        Optional<Subject> result = subjectRepository.findById(id);

        if (result.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy môn học có id: " + id
            );
        }

        return result.get();
    }

    private ChapterResponse convertToResponse(Chapter chapter) {
        ChapterResponse response = new ChapterResponse();

        response.setId(chapter.getId());
        response.setName(chapter.getName());
        response.setOrderIndex(chapter.getOrderIndex());
        response.setSubjectId(chapter.getSubject().getId());

        return response;
    }
}