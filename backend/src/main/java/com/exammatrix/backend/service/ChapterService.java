package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.request.ChapterRequest;
import com.exammatrix.backend.dto.response.ChapterResponse;

import java.util.List;

public interface ChapterService {
    List<ChapterResponse> getAllChapters(Integer subjectId);

    ChapterResponse getDetailChapter(Integer id);

    ChapterResponse createNewChapter(ChapterRequest request);

    ChapterResponse updateChapterById(Integer id, ChapterRequest request);

    void deleteChapterById(Integer id);
}