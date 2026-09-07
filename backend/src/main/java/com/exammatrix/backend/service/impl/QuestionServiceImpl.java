package com.exammatrix.backend.service.impl;

import com.exammatrix.backend.dto.request.AnswerRequest;
import com.exammatrix.backend.dto.request.QuestionRequest;
import com.exammatrix.backend.dto.response.AnswerResponse;
import com.exammatrix.backend.dto.response.PageResponse;
import com.exammatrix.backend.dto.response.QuestionAvailabilityResponse;
import com.exammatrix.backend.dto.response.QuestionResponse;
import com.exammatrix.backend.entity.Answer;
import com.exammatrix.backend.entity.Chapter;
import com.exammatrix.backend.entity.Question;
import com.exammatrix.backend.entity.User;
import com.exammatrix.backend.entity.enums.Difficulty;
import com.exammatrix.backend.entity.enums.QuestionType;
import com.exammatrix.backend.repository.ChapterRepository;
import com.exammatrix.backend.repository.QuestionRepository;
import com.exammatrix.backend.repository.SubjectRepository;
import com.exammatrix.backend.security.CurrentUserService;
import com.exammatrix.backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;
    private final CurrentUserService currentUserService;

    @Override
    public PageResponse<QuestionResponse> getAllQuestions(
            String search,
            Integer subjectId,
            Integer chapterId,
            Difficulty difficulty,
            QuestionType type,
            Integer page,
            Integer size
    ) {
        currentUserService.getCurrentUser();

        int safePage = page == null || page < 0 ? 0 : page;

        int safeSize = size == null || size < 1 ? 10 : Math.min(size, 100);

        String normalizedSearch = normalizeFilter(search);

        Pageable pageable = PageRequest.of(
            safePage,
            safeSize,
            Sort.by(Sort.Order.desc("createdAt"))
        );

        Page<Question> questionPage =
                questionRepository.searchQuestions(
                    normalizedSearch,
                    subjectId,
                    chapterId,
                    difficulty,
                    type,
                    pageable
                );

        List<QuestionResponse> responses = new ArrayList<>();

        for (Question question : questionPage.getContent()) {
            responses.add(convertToResponse(question));
        }

        return new PageResponse<>(
            responses,
            questionPage.getNumber(),
            questionPage.getSize(),
            questionPage.getTotalElements(),
            questionPage.getTotalPages()
        );
    }

    @Override
    public QuestionResponse getQuestionById(UUID id) {
        currentUserService.getCurrentUser();

        Question question = findQuestionById(id);

        return convertToResponse(question);
    }

    @Override
    public QuestionResponse createQuestion(QuestionRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        validateQuestionRequest(request);

        Chapter chapter = findChapterById(request.getChapterId());

        Question question = new Question();

        question.setChapter(chapter);
        question.setTeacher(currentUser);
        question.setContent(request.getContent().trim());
        question.setDifficulty(request.getDifficulty());
        question.setType(request.getType());

        addAnswersToQuestion(question, request.getAnswers());

        Question savedQuestion = questionRepository.save(question);

        return convertToResponse(savedQuestion);
    }

    @Override
    public QuestionResponse updateQuestion(UUID id, QuestionRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Question question = findQuestionById(id);

        checkCanModify(question, currentUser);

        validateQuestionRequest(request);

        Chapter chapter = findChapterById(request.getChapterId());

        question.setChapter(chapter);
        question.setContent(request.getContent().trim());
        question.setDifficulty(request.getDifficulty());
        question.setType(request.getType());

        question.getAnswers().clear();

        addAnswersToQuestion(question, request.getAnswers());

        Question savedQuestion = questionRepository.save(question);

        return convertToResponse(savedQuestion);
    }

    @Override
    public void deleteQuestion(UUID id) {
        User currentUser = currentUserService.getCurrentUser();

        Question question = findQuestionById(id);

        checkCanModify(question, currentUser);

        try {
            questionRepository.delete(question);

            questionRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa câu hỏi đang được sử dụng trong đề thi"
            );
        }
    }

    @Override
    public List<QuestionAvailabilityResponse> getAvailability(Integer subjectId) {
        currentUserService.getCurrentUser();

        if (subjectId == null || subjectId < 1) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID môn học không hợp lệ"
            );
        }

        if (!subjectRepository.existsById(subjectId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Môn học không tồn tại"
            );
        }

        List<Chapter> chapters = chapterRepository.findAllBySubjectIdOrderByOrderIndexAsc(subjectId);

        List<QuestionAvailabilityResponse> responses = new ArrayList<>();

        for (Chapter chapter : chapters) {
            for (Difficulty difficulty : Difficulty.values()) {
                long available = questionRepository.countByChapter_IdAndDifficulty(
                        chapter.getId(),
                        difficulty
                );

                responses.add(new QuestionAvailabilityResponse(
                        chapter.getId(),
                        difficulty,
                        available
                    )
                );
            }
        }

        return responses;
    }

    private void validateQuestionRequest(QuestionRequest request) {
        if (request.getAnswers() == null || request.getAnswers().size() < 2) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Câu hỏi phải có ít nhất 2 đáp án"
            );
        }

        if (request.getType() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Loại câu hỏi không hợp lệ"
            );
        }

        long correctAnswerCount = request.getAnswers()
                .stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();

        switch (request.getType()) {
            case SINGLE_CHOICE -> {
                if (correctAnswerCount != 1) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Câu hỏi một lựa chọn phải có đúng 1 đáp án đúng"
                    );
                }
            }

            case MULTIPLE_CHOICE -> {
                if (correctAnswerCount < 1) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Câu hỏi nhiều lựa chọn phải có ít nhất 1 đáp án đúng"
                    );
                }
            }

            case TRUE_FALSE -> validateTrueFalseAnswers(
                request.getAnswers(),
                correctAnswerCount
            );
        }
    }

    private void validateTrueFalseAnswers(
            List<AnswerRequest> answers,
            long correctAnswerCount
    ) {
        if (answers.size() != 2) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Câu hỏi đúng/sai phải có đúng 2 đáp án"
            );
        }

        Set<String> answerContents = answers.stream()
                .map(answer -> answer.getContent()
                    .trim()
                    .toLowerCase(Locale.ROOT)
                )
                .collect(Collectors.toSet());

        boolean containsTrue = answerContents.contains("đúng");

        boolean containsFalse = answerContents.contains("sai");

        if (!containsTrue || !containsFalse) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Câu hỏi đúng/sai phải có hai đáp án Đúng và Sai"
            );
        }

        if (correctAnswerCount != 1) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Câu hỏi đúng/sai phải có đúng 1 đáp án đúng"
            );
        }
    }

    private void addAnswersToQuestion(Question question, List<AnswerRequest> requests) {
        for (AnswerRequest request : requests) {
            Answer answer = new Answer();

            answer.setContent(request.getContent().trim());
            answer.setIsCorrect(
                Boolean.TRUE.equals(
                    request.getIsCorrect()
                )
            );

            question.addAnswer(answer);
        }
    }

    private void checkCanModify(Question question, User currentUser) {
        String roleName = currentUser
                .getRole()
                .getName();

        boolean isAdmin = "ADMIN".equalsIgnoreCase(roleName);

        boolean isTeacherOwner = "TEACHER".equalsIgnoreCase(roleName)
                    && question.getTeacher()
                    .getId()
                    .equals(currentUser.getId());

        if (!isAdmin && !isTeacherOwner) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Bạn không có quyền sửa hoặc xóa câu hỏi này"
            );
        }
    }

    private Question findQuestionById(UUID id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy câu hỏi có ID " + id
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

    private QuestionResponse convertToResponse(Question question) {
        List<AnswerResponse> answerResponses = new ArrayList<>();

        for (Answer answer : question.getAnswers()) {
            answerResponses.add(new AnswerResponse(
                    answer.getId(),
                    answer.getContent(),
                    answer.getIsCorrect()
                )
            );
        }

        return new QuestionResponse(
            question.getId(),
            question.getChapter().getId(),
            question.getContent(),
            question.getTeacher().getId(),
            question.getDifficulty(),
            question.getType(),
            answerResponses,
            question.getCreatedAt()
        );
    }
}