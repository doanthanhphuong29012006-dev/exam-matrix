package com.exammatrix.backend.service.impl;

import com.exammatrix.backend.dto.request.ChapterRequest;
import com.exammatrix.backend.dto.response.ChapterResponse;
import com.exammatrix.backend.entity.Chapter;
import com.exammatrix.backend.entity.Subject;
import com.exammatrix.backend.repository.ChapterRepository;
import com.exammatrix.backend.repository.SubjectRepository;
import com.exammatrix.backend.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;

    @Override
    public ChapterResponse createNewChapter(ChapterRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Môn học không tồn tại!"));

        Chapter newChapter = Chapter.builder()
                .name(request.getName())
                .orderIndex(request.getOrderIndex())
                .subject(subject)
                .build();

        Chapter savedChapter = chapterRepository.save(newChapter);

        return ChapterResponse.builder()
                .id(savedChapter.getId())
                .name(savedChapter.getName())
                .orderIndex(savedChapter.getOrderIndex())
                .subjectId(savedChapter.getSubject().getId())
                .build();
    }
}