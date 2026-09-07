package com.exammatrix.backend.service.impl;

import com.exammatrix.backend.dto.request.ExamMatrixRequest;
import com.exammatrix.backend.dto.request.MatrixConfigRequest;
import com.exammatrix.backend.dto.response.ExamMatrixResponse;
import com.exammatrix.backend.dto.response.MatrixConfigResponse;
import com.exammatrix.backend.dto.response.PageResponse;
import com.exammatrix.backend.entity.*;
import com.exammatrix.backend.repository.ChapterRepository;
import com.exammatrix.backend.repository.ExamMatrixRepository;
import com.exammatrix.backend.repository.QuestionRepository;
import com.exammatrix.backend.repository.SubjectRepository;
import com.exammatrix.backend.security.CurrentUserService;
import com.exammatrix.backend.service.ExamMatrixService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ExamMatrixServiceImpl implements ExamMatrixService {
    private final ExamMatrixRepository examMatrixRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final CurrentUserService currentUserService;

    @Override
    public PageResponse<ExamMatrixResponse> getAllExamMatrices(
            String search,
            Integer subjectId,
            Integer page,
            Integer size
    ) {
        User currentUser = currentUserService.getCurrentUser();

        int safePage = page == null || page < 0 ? 0 : page;

        int safeSize = size == null || size < 1 ? 10 : Math.min(size, 100);

        String normalizedSearch = normalizeFilter(search);

        UUID teacherFilter = null;

        if (!isAdmin(currentUser)) {
            teacherFilter = currentUser.getId();
        }

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Order.desc("updatedAt"))
        );

        Page<ExamMatrix> matrixPage = examMatrixRepository.searchExamMatrices(
                normalizedSearch,
                subjectId,
                teacherFilter,
                pageable
        );

        List<ExamMatrixResponse> responses = new ArrayList<>();

        for (ExamMatrix matrix : matrixPage.getContent()) {

            responses.add(convertToResponse(matrix));
        }

        return new PageResponse<>(
                responses,
                matrixPage.getNumber(),
                matrixPage.getSize(),
                matrixPage.getTotalElements(),
                matrixPage.getTotalPages()
        );
    }

    @Override
    public ExamMatrixResponse getExamMatrixById(UUID id) {
        User currentUser = currentUserService.getCurrentUser();

        ExamMatrix matrix = findExamMatrixById(id);

        checkCanAccess(matrix, currentUser);

        return convertToResponse(matrix);
    }

    @Override
    public ExamMatrixResponse createExamMatrix(ExamMatrixRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Subject subject = findSubjectById(request.getSubjectId());

        ExamMatrix matrix = new ExamMatrix();

        matrix.setTeacher(currentUser);
        matrix.setSubject(subject);
        matrix.setTitle(request.getTitle().trim());
        matrix.setDuration(request.getDuration());
        matrix.setTotalQuestions(request.getTotalQuestions());

        addAndValidateConfigs(matrix, request);

        ExamMatrix savedMatrix = examMatrixRepository.save(matrix);

        return convertToResponse(savedMatrix);
    }

    @Override
    public ExamMatrixResponse updateExamMatrix(UUID id, ExamMatrixRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        ExamMatrix matrix = findExamMatrixById(id);

        checkCanModify(matrix, currentUser);

        Subject subject = findSubjectById(request.getSubjectId());

        matrix.setSubject(subject);
        matrix.setTitle(request.getTitle().trim());
        matrix.setDuration(request.getDuration());
        matrix.setTotalQuestions(request.getTotalQuestions());

        matrix.getConfigs().clear();

        addAndValidateConfigs(matrix, request);

        ExamMatrix savedMatrix = examMatrixRepository.save(matrix);

        return convertToResponse(savedMatrix);
    }

    @Override
    public void deleteExamMatrix(UUID id) {
        User currentUser = currentUserService.getCurrentUser();

        ExamMatrix matrix = findExamMatrixById(id);

        checkCanModify(matrix, currentUser);

        try {
            examMatrixRepository.delete(matrix);
            examMatrixRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa ma trận đã được dùng để sinh đề"
            );
        }
    }

    private void addAndValidateConfigs(ExamMatrix matrix, ExamMatrixRequest request) {
        if (request.getConfigs() == null || request.getConfigs().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ma trận phải có ít nhất một cấu hình"
            );
        }

        Set<String> usedConfigs = new HashSet<>();

        long totalQuantity = 0;

        for (MatrixConfigRequest configRequest : request.getConfigs()) {

            if (configRequest.getQuantity() == null || configRequest.getQuantity() < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Số lượng câu hỏi không được âm"
                );
            }

            if (configRequest.getQuantity() == 0) {
                continue;
            }

            if (configRequest.getDifficulty() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Độ khó không hợp lệ"
                );
            }

            Chapter chapter = findChapterById(configRequest.getChapterId());

            boolean chapterBelongsToSubject = chapter.getSubject()
                    .getId()
                    .equals(request.getSubjectId());

            if (!chapterBelongsToSubject) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Chương " + chapter.getId() + " không thuộc môn học đã chọn"
                );
            }

            String configKey = chapter.getId()
                            + ":"
                            + configRequest
                            .getDifficulty()
                            .name();

            if (!usedConfigs.add(configKey)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cấu hình chương và độ khó bị trùng"
                );
            }

            long availableQuestions = questionRepository.countByChapter_IdAndDifficulty(
                    chapter.getId(),
                    configRequest.getDifficulty()
            );

            if (configRequest.getQuantity() > availableQuestions) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Chương " + chapter.getName() + " chỉ có " + availableQuestions + " câu " + configRequest.getDifficulty());
            }

            ExamMatrixConfig config = new ExamMatrixConfig();

            config.setChapter(chapter);
            config.setDifficulty(configRequest.getDifficulty());
            config.setQuantity(configRequest.getQuantity());

            matrix.addConfig(config);

            totalQuantity += configRequest.getQuantity();
        }

        if (matrix.getConfigs().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ma trận phải có ít nhất một cấu hình có số lượng lớn hơn 0"
            );
        }

        if (totalQuantity != request.getTotalQuestions()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tổng số câu trong cấu hình là " + totalQuantity + ", phải bằng " + request.getTotalQuestions()
            );
        }
    }

    private void checkCanAccess(ExamMatrix matrix, User currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }

        boolean isOwner = matrix.getTeacher()
                .getId()
                .equals(currentUser.getId());

        if (!isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền xem ma trận này"
            );
        }
    }

    private void checkCanModify(ExamMatrix matrix, User currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }

        boolean isTeacherOwner =
                "TEACHER".equalsIgnoreCase(currentUser.getRole().getName())
                        && matrix.getTeacher()
                        .getId()
                        .equals(currentUser.getId());

        if (!isTeacherOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền sửa hoặc xóa ma trận này"
            );
        }
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole().getName());
    }

    private ExamMatrix findExamMatrixById(UUID id) {
        return examMatrixRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy ma trận có ID " + id
                        )
                );
    }

    private Subject findSubjectById(Integer id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy môn học có ID " + id
                        )
                );
    }

    private Chapter findChapterById(Integer id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy chương có ID " + id
                        )
                );
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private ExamMatrixResponse convertToResponse(ExamMatrix matrix) {
        List<ExamMatrixConfig> sortedConfigs = new ArrayList<>(matrix.getConfigs());

        sortedConfigs.sort(Comparator
                .comparing((ExamMatrixConfig config) -> config.getChapter().getOrderIndex())
                .thenComparing(config -> config.getDifficulty().ordinal())
        );

        List<MatrixConfigResponse> configResponses = new ArrayList<>();

        for (ExamMatrixConfig config : sortedConfigs) {

            configResponses.add(new MatrixConfigResponse(
                            config.getChapter().getId(),
                            config.getDifficulty(),
                            config.getQuantity()
                    )
            );
        }

        return new ExamMatrixResponse(
                matrix.getId(),
                matrix.getTeacher().getId(),
                matrix.getSubject().getId(),
                matrix.getTitle(),
                matrix.getDuration(),
                matrix.getTotalQuestions(),
                configResponses,
                matrix.getCreatedAt(),
                matrix.getUpdatedAt()
        );
    }
}