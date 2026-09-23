package com.exammatrix.backend.service.impl;

import com.exammatrix.backend.dto.request.GenerateExamPapersRequest;
import com.exammatrix.backend.dto.response.AnswerResponse;
import com.exammatrix.backend.dto.response.ExamPaperDetailResponse;
import com.exammatrix.backend.dto.response.PaperQuestionResponse;
import com.exammatrix.backend.entity.*;
import com.exammatrix.backend.entity.enums.ExamType;
import com.exammatrix.backend.entity.enums.QuestionType;
import com.exammatrix.backend.repository.ExamMatrixRepository;
import com.exammatrix.backend.repository.ExamPaperRepository;
import com.exammatrix.backend.repository.QuestionRepository;
import com.exammatrix.backend.security.CurrentUserService;
import com.exammatrix.backend.service.ExamGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExamGenerationServiceImpl implements ExamGenerationService {
    private final ExamPaperRepository examPaperRepository;
    private final ExamMatrixRepository examMatrixRepository;
    private final QuestionRepository questionRepository;
    private final CurrentUserService currentUserService;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final List<QuestionType> OBJECTIVE_TYPES = List.of(
            QuestionType.SINGLE_CHOICE,
            QuestionType.MULTIPLE_CHOICE,
            QuestionType.TRUE_FALSE
    );

    private static final List<QuestionType> ESSAY_TYPES = List.of(
            QuestionType.ESSAY
    );

    @Override
    @Transactional
    public List<ExamPaperDetailResponse> generateExamPapers(
            UUID matrixId,
            GenerateExamPapersRequest request
    ) {
        User currentUser = currentUserService.getCurrentUser();

        ExamMatrix matrix = examMatrixRepository.findById(matrixId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy ma trận có ID " + matrixId
                ));

        checkCanAccess(matrix, currentUser);

        if (request.getNumberOfPapers() == null || request.getNumberOfPapers() < 1 || request.getNumberOfPapers() > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số mã đề phải từ 1 đến 20"
            );
        }

        int numberOfPapers = request.getNumberOfPapers();
        List<ExamPaper> generatedPapers = new ArrayList<>();
        Set<String> codesInCurrentRequest = new HashSet<>();

        for (int index = 0; index < numberOfPapers; index++) {
            List<Question> selectedQuestions = selectQuestionsForPaper(matrix);

            ExamPaper paper = new ExamPaper();
            paper.setExamMatrix(matrix);
            paper.setExamCode(generateExamCode(matrix.getId(), codesInCurrentRequest));

            for (int qIndex = 0; qIndex < selectedQuestions.size(); qIndex++) {
                Question question = selectedQuestions.get(qIndex);

                PaperQuestion paperQuestion = new PaperQuestion();
                paperQuestion.setQuestion(question);
                paperQuestion.setQuestionOrder(qIndex + 1);

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

    private List<Question> selectQuestionsForPaper(ExamMatrix matrix) {
        List<Question> selectedQuestions = new ArrayList<>();
        Set<UUID> selectedQuestionIds = new HashSet<>();

        ExamType examType = matrix.getExamType() != null ? matrix.getExamType() : ExamType.OBJECTIVE;
        List<QuestionType> allowedTypes = (examType == ExamType.ESSAY) ? ESSAY_TYPES : OBJECTIVE_TYPES;

        for (ExamMatrixConfig config : matrix.getConfigs()) {
            List<Question> candidates = new ArrayList<>(
                    questionRepository.findAllByChapter_IdAndDifficultyAndTypeIn(
                            config.getChapter().getId(),
                            config.getDifficulty(),
                            allowedTypes
                    )
            );

            if (candidates.size() < config.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Chương " + config.getChapter().getName()
                                + " không đủ câu hỏi " + examType
                                + " mức " + config.getDifficulty()
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
                        "Không chọn đủ câu hỏi không trùng lặp cho cấu hình ma trận"
                );
            }
        }

        if (selectedQuestions.size() != matrix.getTotalQuestions()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Số câu chọn được không bằng tổng số câu ma trận yêu cầu"
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

            boolean alreadyUsed = codesInCurrentRequest.contains(code)
                    || examPaperRepository.existsByExamMatrix_IdAndExamCode(matrixId, code);

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

    private void checkCanAccess(ExamMatrix matrix, User currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }

        boolean isTeacherOwner = "TEACHER".equalsIgnoreCase(currentUser.getRole().getName())
                && matrix.getTeacher().getId().equals(currentUser.getId());

        if (!isTeacherOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền sinh đề cho ma trận này"
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
            boolean isEssay = question.getType() == QuestionType.ESSAY;

            List<AnswerResponse> answerResponses = new ArrayList<>();
            String referenceAnswer = null;

            if (isEssay) {
                referenceAnswer = question.getReferenceAnswer();
            } else {
                List<Answer> answers = new ArrayList<>(question.getAnswers());
                Collections.shuffle(answers, secureRandom);

                for (Answer answer : answers) {
                    answerResponses.add(new AnswerResponse(
                            answer.getId(),
                            answer.getContent(),
                            answer.getIsCorrect()
                    ));
                }
            }

            questionResponses.add(new PaperQuestionResponse(
                    question.getId(),
                    paperQuestion.getQuestionOrder(),
                    question.getContent(),
                    question.getType(),
                    question.getDifficulty(),
                    answerResponses,
                    referenceAnswer
            ));
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
