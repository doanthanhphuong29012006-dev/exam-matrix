package com.exammatrix.backend.service.impl;

import com.exammatrix.backend.dto.request.GenerateExamPapersRequest;
import com.exammatrix.backend.dto.response.*;
import com.exammatrix.backend.entity.*;
import com.exammatrix.backend.repository.ExamMatrixRepository;
import com.exammatrix.backend.repository.ExamPaperRepository;
import com.exammatrix.backend.repository.QuestionRepository;
import com.exammatrix.backend.security.CurrentUserService;
import com.exammatrix.backend.service.ExamPaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamPaperServiceImpl implements ExamPaperService {
    private final ExamPaperRepository examPaperRepository;
    private final ExamMatrixRepository examMatrixRepository;
    private final QuestionRepository questionRepository;
    private final CurrentUserService currentUserService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public PageResponse<ExamPaperDetailResponse> getAllExamPapers(
            UUID matrixId,
            Integer page,
            Integer size
    ) {
        User currentUser = currentUserService.getCurrentUser();

        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size < 1 ? 10 : Math.min(size, 100);

        UUID teacherFilter = isAdmin(currentUser) ? null : currentUser.getId();

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Order.desc("createdAt"))
        );

        Page<ExamPaper> paperPage = examPaperRepository.searchExamPapers(
                matrixId,
                teacherFilter,
                pageable
        );

        List<ExamPaperDetailResponse> responses = new ArrayList<>();

        for (ExamPaper paper : paperPage.getContent()) {
            responses.add(convertToResponse(paper));
        }

        return new PageResponse<>(
                responses,
                paperPage.getNumber(),
                paperPage.getSize(),
                paperPage.getTotalElements(),
                paperPage.getTotalPages()
        );
    }

    @Override
    public ExamPaperDetailResponse getExamPaperById(UUID id) {
        User currentUser = currentUserService.getCurrentUser();

        ExamPaper paper = findExamPaperById(id);

        checkCanAccess(paper.getExamMatrix(), currentUser);

        return convertToResponse(paper);
    }

    @Override
    @Transactional
    public List<ExamPaperDetailResponse> generateExamPapers(
            UUID matrixId,
            GenerateExamPapersRequest request
    ) {
        User currentUser = currentUserService.getCurrentUser();

        ExamMatrix matrix = examMatrixRepository.findByIdForUpdate(matrixId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy ma trận có ID " + matrixId
                ));

        checkCanAccess(matrix, currentUser);

        int numberOfPapers = request.getNumberOfPapers();

        List<ExamPaper> generatedPapers = new ArrayList<>();
        Set<String> codesInCurrentRequest = new HashSet<>();

        for (int index = 0; index < numberOfPapers; index++) {
            List<Question> selectedQuestions = selectQuestionsForPaper(matrix);

            ExamPaper paper = new ExamPaper();

            paper.setExamMatrix(matrix);
            paper.setExamCode(generateExamCode(matrix.getId(), codesInCurrentRequest));

            for (int questionIndex = 0; questionIndex < selectedQuestions.size(); questionIndex++) {

                Question question = selectedQuestions.get(questionIndex);

                PaperQuestion paperQuestion = new PaperQuestion();

                paperQuestion.setQuestion(question);
                paperQuestion.setQuestionOrder(questionIndex + 1);

                paper.addPaperQuestion(paperQuestion);
            }

            ExamPaper savedPaper = examPaperRepository.save(paper);

            generatedPapers.add(savedPaper);
        }

        examPaperRepository.flush();

        List<ExamPaperDetailResponse> responses = new ArrayList<>();

        for (ExamPaper paper : generatedPapers) {
            responses.add(convertToResponse(paper));
        }

        return responses;
    }

    @Override
    @Transactional
    public void deleteExamPaper(UUID id) {
        User currentUser = currentUserService.getCurrentUser();

        ExamPaper paper = findExamPaperById(id);

        checkCanAccess(paper.getExamMatrix(), currentUser);

        examPaperRepository.delete(paper);
        examPaperRepository.flush();
    }

    private List<Question> selectQuestionsForPaper(ExamMatrix matrix) {
        List<Question> selectedQuestions = new ArrayList<>();

        Set<UUID> selectedQuestionIds = new HashSet<>();

        for (ExamMatrixConfig config : matrix.getConfigs()) {
            List<Question> candidates = new ArrayList<>(questionRepository.findAllByChapter_IdAndDifficulty(
                        config.getChapter().getId(),
                        config.getDifficulty()
                )
            );

            if (candidates.size() < config.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Chương "
                        + config.getChapter().getName()
                        + " không đủ câu hỏi mức "
                        + config.getDifficulty()
                );
            }

            Collections.shuffle(candidates, secureRandom);

            int added = 0;

            for (Question candidate : candidates) {
                if (selectedQuestionIds.add(candidate.getId())) {
                    selectedQuestions.add(candidate);
                    added++;
                }

                if (added == config.getQuantity()) {
                    break;
                }
            }

            if (added < config.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Không chọn đủ câu hỏi cho cấu hình ma trận"
                );
            }
        }

        if (selectedQuestions.size() != matrix.getTotalQuestions()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Số câu đã chọn không bằng tổng số câu của ma trận"
            );
        }

        Collections.shuffle(selectedQuestions, secureRandom);

        return selectedQuestions;
    }

    private String generateExamCode(UUID matrixId, Set<String> codesInCurrentRequest) {
        for (int attempt = 0; attempt < 100; attempt++) {
            String code = String.format(
                    Locale.ROOT,
                    "%06d",
                    secureRandom.nextInt(1_000_000)
            );

            boolean alreadyUsed = codesInCurrentRequest.contains(code) || examPaperRepository
                .existsByExamMatrix_IdAndExamCode(
                        matrixId,
                        code
            );

            if (!alreadyUsed) {
                codesInCurrentRequest.add(code);
                return code;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể tạo mã đề duy nhất"
        );
    }

    private ExamPaper findExamPaperById(UUID id) {
        return examPaperRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đề thi có ID " + id
                ));
    }

    private void checkCanAccess(ExamMatrix matrix, User currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }

        boolean isTeacherOwner = "TEACHER".equalsIgnoreCase(currentUser.getRole().getName())
                        && matrix.getTeacher()
                        .getId()
                        .equals(currentUser.getId());

        if (!isTeacherOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền truy cập đề thi này"
            );
        }
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole().getName());
    }

    private ExamPaperDetailResponse convertToResponse(ExamPaper paper) {
        List<PaperQuestion> sortedQuestions = new ArrayList<>(paper.getPaperQuestions());

        sortedQuestions.sort(Comparator.comparing(PaperQuestion::getQuestionOrder));

        List<PaperQuestionResponse> questionResponses = new ArrayList<>();

        for (PaperQuestion paperQuestion : sortedQuestions) {
            Question question = paperQuestion.getQuestion();

            List<AnswerResponse> answerResponses = new ArrayList<>();

            for (Answer answer : question.getAnswers()) {
                answerResponses.add(new AnswerResponse(
                            answer.getId(),
                            answer.getContent(),
                            answer.getIsCorrect()
                    )
                );
            }

            questionResponses.add(new PaperQuestionResponse(
                        question.getId(),
                        paperQuestion.getQuestionOrder(),
                        question.getContent(),
                        question.getType(),
                        question.getDifficulty(),
                        answerResponses
                )
            );
        }

        return new ExamPaperDetailResponse(
                paper.getId(),
                paper.getExamMatrix().getId(),
                paper.getExamCode(),
                paper.getCreatedAt(),
                questionResponses
        );
    }
}