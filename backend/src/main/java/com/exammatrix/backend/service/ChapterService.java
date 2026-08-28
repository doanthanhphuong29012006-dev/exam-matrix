package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.request.ChapterRequest;
import com.exammatrix.backend.dto.response.ChapterResponse;

public interface ChapterService {
    ChapterResponse createNewChapter(ChapterRequest request);
}