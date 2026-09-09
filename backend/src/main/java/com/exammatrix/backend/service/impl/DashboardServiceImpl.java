package com.exammatrix.backend.service.impl;

import com.exammatrix.backend.dto.response.*;
import com.exammatrix.backend.dto.response.DashboardResponse;
import com.exammatrix.backend.entity.ExamMatrix;
import com.exammatrix.backend.entity.ExamMatrixConfig;
import com.exammatrix.backend.entity.User;
import com.exammatrix.backend.entity.enums.Difficulty;
import com.exammatrix.backend.repository.*;
import com.exammatrix.backend.security.CurrentUserService;
import com.exammatrix.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final ExamMatrixRepository examMatrixRepository;
    private final ExamPaperRepository examPaperRepository;
    private final CurrentUserService currentUserService;

    @Override
    public DashboardResponse getSummary() {
        User currentUser = currentUserService.getCurrentUser();

        boolean admin = isAdmin(currentUser);

        long totalSubjects = subjectRepository.count();
        long totalChapters = chapterRepository.count();
        long totalQuestions = questionRepository.count();

        long totalMatrices;
        long totalPapers;
        List<ExamMatrix> recentMatrices;

        if (admin) {
            totalMatrices = examMatrixRepository.count();
            totalPapers = examPaperRepository.count();

            recentMatrices = examMatrixRepository.findTop5ByOrderByUpdatedAtDesc();
        } else {
            totalMatrices = examMatrixRepository.countByTeacher_Id(currentUser.getId());

            totalPapers = examPaperRepository.countByExamMatrix_Teacher_Id(currentUser.getId());

            recentMatrices = examMatrixRepository.findTop5ByTeacher_IdOrderByUpdatedAtDesc(currentUser.getId());
        }

        List<DifficultyCountResponse> difficulty = new ArrayList<>();

        for (Difficulty value : Difficulty.values()) {
            long count = questionRepository.countByDifficulty(value);

            difficulty.add(new DifficultyCountResponse(value, count));
        }

        List<ExamMatrixResponse> recentResponses = new ArrayList<>();

        for (ExamMatrix matrix : recentMatrices) {
            recentResponses.add(convertMatrixToResponse(matrix));
        }

        return new DashboardResponse(
                totalSubjects,
                totalChapters,
                totalQuestions,
                totalMatrices,
                totalPapers,
                difficulty,
                recentResponses
        );
    }

    private ExamMatrixResponse convertMatrixToResponse(ExamMatrix matrix) {
        List<ExamMatrixConfig> sortedConfigs = new ArrayList<>(matrix.getConfigs());

        sortedConfigs.sort(Comparator.comparing((ExamMatrixConfig config) -> config.getChapter().getOrderIndex())
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

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(
                user.getRole().getName()
        );
    }
}