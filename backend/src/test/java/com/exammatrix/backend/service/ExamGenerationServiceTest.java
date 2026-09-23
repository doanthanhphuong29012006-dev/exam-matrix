package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.request.GenerateExamPapersRequest;
import com.exammatrix.backend.dto.response.ExamPaperDetailResponse;
import com.exammatrix.backend.dto.response.PaperQuestionResponse;
import com.exammatrix.backend.entity.*;
import com.exammatrix.backend.entity.enums.Difficulty;
import com.exammatrix.backend.entity.enums.ExamType;
import com.exammatrix.backend.entity.enums.QuestionType;
import com.exammatrix.backend.repository.ExamMatrixRepository;
import com.exammatrix.backend.repository.ExamPaperRepository;
import com.exammatrix.backend.repository.QuestionRepository;
import com.exammatrix.backend.security.CurrentUserService;
import com.exammatrix.backend.service.impl.ExamGenerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamGenerationServiceTest {

    @Mock
    private ExamPaperRepository examPaperRepository;

    @Mock
    private ExamMatrixRepository examMatrixRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ExamGenerationServiceImpl examGenerationService;

    private User teacherUser;
    private Role teacherRole;
    private Subject subject;
    private Chapter chapter;
    private ExamMatrix matrixObjective;
    private ExamMatrix matrixEssay;
    private UUID matrixId;

    @BeforeEach
    void setUp() {
        matrixId = UUID.randomUUID();

        teacherRole = new Role();
        teacherRole.setId(1);
        teacherRole.setName("TEACHER");

        teacherUser = new User();
        teacherUser.setId(UUID.randomUUID());
        teacherUser.setUsername("teacher1");
        teacherUser.setRole(teacherRole);

        subject = new Subject();
        subject.setId(1);
        subject.setName("Lap trinh Java");

        chapter = new Chapter();
        chapter.setId(10);
        chapter.setName("Chuong 1: Tong quan");
        chapter.setSubject(subject);

        // Ma tran Trac nghiem (OBJECTIVE)
        matrixObjective = new ExamMatrix();
        matrixObjective.setId(matrixId);
        matrixObjective.setTeacher(teacherUser);
        matrixObjective.setSubject(subject);
        matrixObjective.setTitle("De thi Trac nghiem OOP");
        matrixObjective.setDuration(60);
        matrixObjective.setTotalQuestions(2);
        matrixObjective.setExamType(ExamType.OBJECTIVE);

        ExamMatrixConfig configObj = new ExamMatrixConfig();
        configObj.setChapter(chapter);
        configObj.setDifficulty(Difficulty.EASY);
        configObj.setQuantity(2);
        matrixObjective.addConfig(configObj);

        // Ma tran Tu luan (ESSAY)
        matrixEssay = new ExamMatrix();
        matrixEssay.setId(matrixId);
        matrixEssay.setTeacher(teacherUser);
        matrixEssay.setSubject(subject);
        matrixEssay.setTitle("De thi Tu luan OOP");
        matrixEssay.setDuration(60);
        matrixEssay.setTotalQuestions(1);
        matrixEssay.setExamType(ExamType.ESSAY);

        ExamMatrixConfig configEssay = new ExamMatrixConfig();
        configEssay.setChapter(chapter);
        configEssay.setDifficulty(Difficulty.MEDIUM);
        configEssay.setQuantity(1);
        matrixEssay.addConfig(configEssay);
    }

    @Test
    @DisplayName("Sinh đề trắc nghiệm thành công với đủ đáp án và mã đề 6 chữ số")
    void generateExamPapers_Success_Objective() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherUser);
        when(examMatrixRepository.findById(matrixId)).thenReturn(Optional.of(matrixObjective));

        Question q1 = createQuestion(QuestionType.SINGLE_CHOICE, Difficulty.EASY, "Cau 1?", null);
        q1.addAnswer(createAnswer("Dap an A", true));
        q1.addAnswer(createAnswer("Dap an B", false));

        Question q2 = createQuestion(QuestionType.MULTIPLE_CHOICE, Difficulty.EASY, "Cau 2?", null);
        q2.addAnswer(createAnswer("Dap an A", true));
        q2.addAnswer(createAnswer("Dap an B", true));

        when(questionRepository.findAllByChapter_IdAndDifficultyAndTypeIn(
                eq(10), eq(Difficulty.EASY), anyList()
        )).thenReturn(List.of(q1, q2));

        when(examPaperRepository.existsByExamMatrix_IdAndExamCode(any(), any())).thenReturn(false);
        when(examPaperRepository.save(any(ExamPaper.class))).thenAnswer(invocation -> {
            ExamPaper paper = invocation.getArgument(0);
            paper.setId(UUID.randomUUID());
            paper.setCreatedAt(LocalDateTime.now());
            return paper;
        });

        GenerateExamPapersRequest request = new GenerateExamPapersRequest(2);
        List<ExamPaperDetailResponse> responses = examGenerationService.generateExamPapers(matrixId, request);

        assertNotNull(responses);
        assertEquals(2, responses.size());

        for (ExamPaperDetailResponse paper : responses) {
            assertEquals(matrixId, paper.getMatrixId());
            assertNotNull(paper.getExamCode());
            assertEquals(6, paper.getExamCode().length());
            assertEquals(2, paper.getQuestions().size());

            for (PaperQuestionResponse pq : paper.getQuestions()) {
                assertNull(pq.getReferenceAnswer());
                assertNotNull(pq.getAnswers());
                assertFalse(pq.getAnswers().isEmpty());
            }
        }

        verify(examPaperRepository, times(2)).save(any(ExamPaper.class));
    }

    @Test
    @DisplayName("Sinh đề tự luận thành công có referenceAnswer và answers rỗng []")
    void generateExamPapers_Success_Essay() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherUser);
        when(examMatrixRepository.findById(matrixId)).thenReturn(Optional.of(matrixEssay));

        Question essayQ = createQuestion(
                QuestionType.ESSAY,
                Difficulty.MEDIUM,
                "Phan biet Class va Object?",
                "Class la khuon mau, Object la the hiện."
        );

        when(questionRepository.findAllByChapter_IdAndDifficultyAndTypeIn(
                eq(10), eq(Difficulty.MEDIUM), anyList()
        )).thenReturn(List.of(essayQ));

        when(examPaperRepository.existsByExamMatrix_IdAndExamCode(any(), any())).thenReturn(false);
        when(examPaperRepository.save(any(ExamPaper.class))).thenAnswer(invocation -> {
            ExamPaper paper = invocation.getArgument(0);
            paper.setId(UUID.randomUUID());
            paper.setCreatedAt(LocalDateTime.now());
            return paper;
        });

        GenerateExamPapersRequest request = new GenerateExamPapersRequest(1);
        List<ExamPaperDetailResponse> responses = examGenerationService.generateExamPapers(matrixId, request);

        assertNotNull(responses);
        assertEquals(1, responses.size());

        ExamPaperDetailResponse paper = responses.get(0);
        assertEquals(1, paper.getQuestions().size());

        PaperQuestionResponse pq = paper.getQuestions().get(0);
        assertEquals(QuestionType.ESSAY, pq.getType());
        assertEquals("Class la khuon mau, Object la the hiện.", pq.getReferenceAnswer());
        assertTrue(pq.getAnswers().isEmpty());
    }

    @Test
    @DisplayName("Báo lỗi 404 NOT_FOUND khi không tìm thấy ma trận")
    void generateExamPapers_NotFound() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherUser);
        when(examMatrixRepository.findById(matrixId)).thenReturn(Optional.empty());

        GenerateExamPapersRequest request = new GenerateExamPapersRequest(1);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> examGenerationService.generateExamPapers(matrixId, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("Báo lỗi 403 FORBIDDEN khi người dùng không sở hữu ma trận")
    void generateExamPapers_Forbidden() {
        User otherTeacher = new User();
        otherTeacher.setId(UUID.randomUUID());
        otherTeacher.setRole(teacherRole);

        when(currentUserService.getCurrentUser()).thenReturn(otherTeacher);
        when(examMatrixRepository.findById(matrixId)).thenReturn(Optional.of(matrixObjective));

        GenerateExamPapersRequest request = new GenerateExamPapersRequest(1);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> examGenerationService.generateExamPapers(matrixId, request)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    @DisplayName("Báo lỗi 409 CONFLICT khi kho câu hỏi không đủ số lượng")
    void generateExamPapers_Conflict_NotEnoughQuestions() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherUser);
        when(examMatrixRepository.findById(matrixId)).thenReturn(Optional.of(matrixObjective));

        // Ma tran yeu cau 2 cau, nhung kho chi co 1 cau
        Question q1 = createQuestion(QuestionType.SINGLE_CHOICE, Difficulty.EASY, "Cau 1?", null);
        when(questionRepository.findAllByChapter_IdAndDifficultyAndTypeIn(
                eq(10), eq(Difficulty.EASY), anyList()
        )).thenReturn(List.of(q1));

        GenerateExamPapersRequest request = new GenerateExamPapersRequest(1);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> examGenerationService.generateExamPapers(matrixId, request)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    @DisplayName("Báo lỗi 400 BAD_REQUEST khi số mã đề không nằm trong khoảng 1 đến 20")
    void generateExamPapers_InvalidNumberOfPapers() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherUser);
        when(examMatrixRepository.findById(matrixId)).thenReturn(Optional.of(matrixObjective));

        GenerateExamPapersRequest request0 = new GenerateExamPapersRequest(0);
        assertThrows(
                ResponseStatusException.class,
                () -> examGenerationService.generateExamPapers(matrixId, request0)
        );

        GenerateExamPapersRequest request25 = new GenerateExamPapersRequest(25);
        assertThrows(
                ResponseStatusException.class,
                () -> examGenerationService.generateExamPapers(matrixId, request25)
        );
    }

    private Question createQuestion(QuestionType type, Difficulty difficulty, String content, String refAnswer) {
        Question q = new Question();
        q.setId(UUID.randomUUID());
        q.setType(type);
        q.setDifficulty(difficulty);
        q.setContent(content);
        q.setReferenceAnswer(refAnswer);
        q.setChapter(chapter);
        q.setTeacher(teacherUser);
        return q;
    }

    private Answer createAnswer(String content, boolean isCorrect) {
        Answer a = new Answer();
        a.setId(UUID.randomUUID());
        a.setContent(content);
        a.setIsCorrect(isCorrect);
        return a;
    }
}
