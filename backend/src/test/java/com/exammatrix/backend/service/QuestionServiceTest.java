package com.exammatrix.backend.service;

import com.exammatrix.backend.dto.request.AnswerRequest;
import com.exammatrix.backend.dto.request.QuestionRequest;
import com.exammatrix.backend.dto.response.QuestionAvailabilityResponse;
import com.exammatrix.backend.dto.response.QuestionResponse;
import com.exammatrix.backend.entity.Chapter;
import com.exammatrix.backend.entity.Question;
import com.exammatrix.backend.entity.Role;
import com.exammatrix.backend.entity.Subject;
import com.exammatrix.backend.entity.User;
import com.exammatrix.backend.entity.enums.Difficulty;
import com.exammatrix.backend.entity.enums.ExamType;
import com.exammatrix.backend.entity.enums.QuestionType;
import com.exammatrix.backend.repository.ChapterRepository;
import com.exammatrix.backend.repository.PaperQuestionRepository;
import com.exammatrix.backend.repository.QuestionRepository;
import com.exammatrix.backend.repository.SubjectRepository;
import com.exammatrix.backend.security.CurrentUserService;
import com.exammatrix.backend.service.impl.QuestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private PaperQuestionRepository paperQuestionRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private User teacherA;
    private User teacherB;
    private Chapter chapter1;
    private Subject subject1;

    @BeforeEach
    void setUp() {
        Role teacherRole = Role.builder().id(1).name("TEACHER").build();

        teacherA = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .username("teacherA")
                .role(teacherRole)
                .build();

        teacherB = User.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .username("teacherB")
                .role(teacherRole)
                .build();

        subject1 = Subject.builder()
                .id(1)
                .code("CS101")
                .name("Computer Science")
                .build();

        chapter1 = Chapter.builder()
                .id(1)
                .name("Chapter 1: Intro")
                .orderIndex(1)
                .subject(subject1)
                .build();
    }

    @Test
    @DisplayName("TC-01: Tạo câu tự luận thiếu đáp án tham khảo -> 400 Bad Request")
    void tc01_createEssayWithoutReferenceAnswer_throwsBadRequest() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherA);

        QuestionRequest request = QuestionRequest.builder()
                .chapterId(1)
                .content("Giải thích nguyên lý SOLID")
                .difficulty(Difficulty.MEDIUM)
                .type(QuestionType.ESSAY)
                .referenceAnswer(null)
                .build();

        assertThatThrownBy(() -> questionService.createQuestion(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).contains("Vui lòng nhập đáp án tham khảo cho câu hỏi tự luận");
                });
    }

    @Test
    @DisplayName("TC-02: Tạo câu tự luận nhưng gửi kèm answers -> 400 Bad Request")
    void tc02_createEssayWithAnswers_throwsBadRequest() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherA);

        AnswerRequest answer = new AnswerRequest();
        answer.setContent("Option A");
        answer.setIsCorrect(true);

        QuestionRequest request = QuestionRequest.builder()
                .chapterId(1)
                .content("Giải thích nguyên lý SOLID")
                .difficulty(Difficulty.MEDIUM)
                .type(QuestionType.ESSAY)
                .referenceAnswer("Đáp án tham khảo mẫu")
                .answers(List.of(answer))
                .build();

        assertThatThrownBy(() -> questionService.createQuestion(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).contains("Câu hỏi tự luận không được có đáp án lựa chọn");
                });
    }

    @Test
    @DisplayName("TC-03: Tạo câu trắc nghiệm kèm referenceAnswer -> 400 Bad Request")
    void tc03_createObjectiveWithReferenceAnswer_throwsBadRequest() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherA);

        AnswerRequest a1 = new AnswerRequest();
        a1.setContent("A");
        a1.setIsCorrect(true);
        AnswerRequest a2 = new AnswerRequest();
        a2.setContent("B");
        a2.setIsCorrect(false);

        QuestionRequest request = QuestionRequest.builder()
                .chapterId(1)
                .content("Câu hỏi trắc nghiệm")
                .difficulty(Difficulty.EASY)
                .type(QuestionType.SINGLE_CHOICE)
                .referenceAnswer("Không được có đáp án tham khảo ở trắc nghiệm")
                .answers(List.of(a1, a2))
                .build();

        assertThatThrownBy(() -> questionService.createQuestion(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).contains("Câu hỏi trắc nghiệm không được có đáp án tham khảo");
                });
    }

    @Test
    @DisplayName("TC-04: Tạo câu tự luận hợp lệ -> Thành công, lưu referenceAnswer và answers rỗng")
    void tc04_createValidEssay_success() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherA);
        when(chapterRepository.findById(1)).thenReturn(Optional.of(chapter1));

        Question saved = Question.builder()
                .id(UUID.randomUUID())
                .chapter(chapter1)
                .teacher(teacherA)
                .content("Trình bày kiến trúc MVC")
                .difficulty(Difficulty.HARD)
                .type(QuestionType.ESSAY)
                .referenceAnswer("Model - View - Controller...")
                .answers(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(questionRepository.save(any(Question.class))).thenReturn(saved);

        QuestionRequest request = QuestionRequest.builder()
                .chapterId(1)
                .content("Trình bày kiến trúc MVC")
                .difficulty(Difficulty.HARD)
                .type(QuestionType.ESSAY)
                .referenceAnswer("Model - View - Controller...")
                .answers(Collections.emptyList())
                .build();

        QuestionResponse response = questionService.createQuestion(request);

        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(QuestionType.ESSAY);
        assertThat(response.getReferenceAnswer()).isEqualTo("Model - View - Controller...");
        assertThat(response.getAnswers()).isEmpty();

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        Question captured = questionCaptor.getValue();
        assertThat(captured.getReferenceAnswer()).isEqualTo("Model - View - Controller...");
        assertThat(captured.getAnswers()).isEmpty();
    }

    @Test
    @DisplayName("TC-05: Lấy chi tiết câu hỏi tự luận -> Trả về answers rỗng và referenceAnswer đầy đủ")
    void tc05_getQuestionById_essayQuestion() {
        UUID id = UUID.randomUUID();
        Question essayQuestion = Question.builder()
                .id(id)
                .chapter(chapter1)
                .teacher(teacherA)
                .content("Chi tiết câu hỏi tự luận")
                .difficulty(Difficulty.MEDIUM)
                .type(QuestionType.ESSAY)
                .referenceAnswer("Hướng dẫn chấm bài tự luận")
                .answers(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(teacherA);
        when(questionRepository.findById(id)).thenReturn(Optional.of(essayQuestion));

        QuestionResponse response = questionService.getQuestionById(id);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getType()).isEqualTo(QuestionType.ESSAY);
        assertThat(response.getReferenceAnswer()).isEqualTo("Hướng dẫn chấm bài tự luận");
        assertThat(response.getAnswers()).isEmpty();
    }

    @Test
    @DisplayName("TC-06: Giảng viên sửa câu hỏi của giảng viên khác -> 403 Forbidden")
    void tc06_teacherUpdatesOtherTeachersQuestion_throwsForbidden() {
        UUID id = UUID.randomUUID();
        Question questionOfTeacherA = Question.builder()
                .id(id)
                .chapter(chapter1)
                .teacher(teacherA)
                .content("Câu hỏi của A")
                .difficulty(Difficulty.EASY)
                .type(QuestionType.ESSAY)
                .referenceAnswer("Ans")
                .answers(new ArrayList<>())
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(teacherB);
        when(questionRepository.findById(id)).thenReturn(Optional.of(questionOfTeacherA));

        QuestionRequest request = QuestionRequest.builder()
                .chapterId(1)
                .content("Sửa câu hỏi")
                .difficulty(Difficulty.EASY)
                .type(QuestionType.ESSAY)
                .referenceAnswer("Ans mới")
                .build();

        assertThatThrownBy(() -> questionService.updateQuestion(id, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                });
    }

    @Test
    @DisplayName("TC-07a: Sửa câu hỏi đã được sử dụng trong đề thi -> 409 Conflict")
    void tc07a_updateQuestionUsedInPaper_throwsConflict() {
        UUID id = UUID.randomUUID();
        Question question = Question.builder()
                .id(id)
                .chapter(chapter1)
                .teacher(teacherA)
                .content("Câu hỏi đã vào đề")
                .difficulty(Difficulty.EASY)
                .type(QuestionType.ESSAY)
                .referenceAnswer("Ans")
                .answers(new ArrayList<>())
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(teacherA);
        when(questionRepository.findById(id)).thenReturn(Optional.of(question));
        when(paperQuestionRepository.existsByQuestion_Id(id)).thenReturn(true);

        QuestionRequest request = QuestionRequest.builder()
                .chapterId(1)
                .content("Sửa câu đã vào đề")
                .difficulty(Difficulty.EASY)
                .type(QuestionType.ESSAY)
                .referenceAnswer("Ans mới")
                .build();

        assertThatThrownBy(() -> questionService.updateQuestion(id, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rse.getReason()).contains("Câu hỏi đã được sử dụng trong đề thi");
                });
    }

    @Test
    @DisplayName("TC-07b: Xóa câu hỏi đã được sử dụng trong đề thi -> 409 Conflict")
    void tc07b_deleteQuestionUsedInPaper_throwsConflict() {
        UUID id = UUID.randomUUID();
        Question question = Question.builder()
                .id(id)
                .chapter(chapter1)
                .teacher(teacherA)
                .content("Câu hỏi đã vào đề")
                .difficulty(Difficulty.EASY)
                .type(QuestionType.ESSAY)
                .referenceAnswer("Ans")
                .answers(new ArrayList<>())
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(teacherA);
        when(questionRepository.findById(id)).thenReturn(Optional.of(question));
        when(paperQuestionRepository.existsByQuestion_Id(id)).thenReturn(true);

        assertThatThrownBy(() -> questionService.deleteQuestion(id))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rse.getReason()).contains("Không thể xóa câu hỏi đang được sử dụng trong đề thi");
                });
    }

    @Test
    @DisplayName("TC-08: Đếm số lượng câu hỏi khả dụng cho đề ESSAY -> Chỉ đếm QuestionType.ESSAY")
    void tc08_getAvailability_forEssayExam() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherA);
        when(subjectRepository.existsById(1)).thenReturn(true);
        when(chapterRepository.findAllBySubjectIdOrderByOrderIndexAsc(1)).thenReturn(List.of(chapter1));

        Set<QuestionType> essaySet = Set.of(QuestionType.ESSAY);
        when(questionRepository.countByChapter_IdAndDifficultyAndTypeIn(eq(1), eq(Difficulty.EASY), eq(essaySet))).thenReturn(5L);
        when(questionRepository.countByChapter_IdAndDifficultyAndTypeIn(eq(1), eq(Difficulty.MEDIUM), eq(essaySet))).thenReturn(3L);
        when(questionRepository.countByChapter_IdAndDifficultyAndTypeIn(eq(1), eq(Difficulty.HARD), eq(essaySet))).thenReturn(0L);

        List<QuestionAvailabilityResponse> results = questionService.getAvailability(1, ExamType.ESSAY);

        assertThat(results).hasSize(3);
        assertThat(results).extracting(QuestionAvailabilityResponse::getDifficulty)
                .containsExactly(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        assertThat(results).extracting(QuestionAvailabilityResponse::getAvailable)
                .containsExactly(5L, 3L, 0L);

        verify(questionRepository, times(3)).countByChapter_IdAndDifficultyAndTypeIn(eq(1), any(), eq(essaySet));
    }

    @Test
    @DisplayName("TC-09: Đếm số lượng câu hỏi khả dụng cho đề OBJECTIVE -> Chỉ đếm SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE")
    void tc09_getAvailability_forObjectiveExam() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherA);
        when(subjectRepository.existsById(1)).thenReturn(true);
        when(chapterRepository.findAllBySubjectIdOrderByOrderIndexAsc(1)).thenReturn(List.of(chapter1));

        Set<QuestionType> objectiveSet = Set.of(
                QuestionType.SINGLE_CHOICE,
                QuestionType.MULTIPLE_CHOICE,
                QuestionType.TRUE_FALSE
        );
        when(questionRepository.countByChapter_IdAndDifficultyAndTypeIn(eq(1), eq(Difficulty.EASY), eq(objectiveSet))).thenReturn(12L);
        when(questionRepository.countByChapter_IdAndDifficultyAndTypeIn(eq(1), eq(Difficulty.MEDIUM), eq(objectiveSet))).thenReturn(8L);
        when(questionRepository.countByChapter_IdAndDifficultyAndTypeIn(eq(1), eq(Difficulty.HARD), eq(objectiveSet))).thenReturn(4L);

        List<QuestionAvailabilityResponse> results = questionService.getAvailability(1, ExamType.OBJECTIVE);

        assertThat(results).hasSize(3);
        assertThat(results).extracting(QuestionAvailabilityResponse::getAvailable)
                .containsExactly(12L, 8L, 4L);

        verify(questionRepository, times(3)).countByChapter_IdAndDifficultyAndTypeIn(eq(1), any(), eq(objectiveSet));
    }

    @Test
    @DisplayName("Nâng cao 1: Tạo câu tự luận có referenceAnswer dài hơn 10000 ký tự -> 400 Bad Request")
    void createEssayWithReferenceAnswerExceeding10000Chars_throwsBadRequest() {
        when(currentUserService.getCurrentUser()).thenReturn(teacherA);

        String tooLongAnswer = "a".repeat(10001);
        QuestionRequest request = QuestionRequest.builder()
                .chapterId(1)
                .content("Giải thích đề thi tự luận")
                .difficulty(Difficulty.HARD)
                .type(QuestionType.ESSAY)
                .referenceAnswer(tooLongAnswer)
                .build();

        assertThatThrownBy(() -> questionService.createQuestion(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).contains("Đáp án tham khảo không được vượt quá 10000 ký tự");
                });
    }

    @Test
    @DisplayName("Nâng cao 2: Xóa câu hỏi gặp vi phạm ràng buộc CSDL lúc flush (Race Condition Lớp 2) -> 409 Conflict")
    void deleteQuestion_databaseIntegrityViolationOnFlush_throwsConflict() {
        UUID id = UUID.randomUUID();
        Question question = Question.builder()
                .id(id)
                .chapter(chapter1)
                .teacher(teacherA)
                .content("Câu hỏi bị race condition")
                .difficulty(Difficulty.EASY)
                .type(QuestionType.ESSAY)
                .referenceAnswer("Ans")
                .answers(new ArrayList<>())
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(teacherA);
        when(questionRepository.findById(id)).thenReturn(Optional.of(question));
        when(paperQuestionRepository.existsByQuestion_Id(id)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("Foreign key violation")).when(questionRepository).flush();

        assertThatThrownBy(() -> questionService.deleteQuestion(id))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rse.getReason()).contains("Không thể xóa câu hỏi đang được sử dụng trong đề thi");
                });
    }

    @Test
    @DisplayName("Nâng cao 3: Cập nhật chuyển từ trắc nghiệm sang tự luận -> Xóa sạch answers cũ và lưu referenceAnswer mới")
    void updateQuestion_convertObjectiveToEssay_clearsAnswersAndSavesReferenceAnswer() {
        UUID id = UUID.randomUUID();
        com.exammatrix.backend.entity.Answer oldAnswer1 = com.exammatrix.backend.entity.Answer.builder().id(UUID.randomUUID()).content("A").isCorrect(true).build();
        com.exammatrix.backend.entity.Answer oldAnswer2 = com.exammatrix.backend.entity.Answer.builder().id(UUID.randomUUID()).content("B").isCorrect(false).build();
        List<com.exammatrix.backend.entity.Answer> answersList = new ArrayList<>(List.of(oldAnswer1, oldAnswer2));

        Question existingQuestion = Question.builder()
                .id(id)
                .chapter(chapter1)
                .teacher(teacherA)
                .content("Câu hỏi ban đầu là trắc nghiệm")
                .difficulty(Difficulty.EASY)
                .type(QuestionType.SINGLE_CHOICE)
                .referenceAnswer(null)
                .answers(answersList)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(teacherA);
        when(questionRepository.findById(id)).thenReturn(Optional.of(existingQuestion));
        when(paperQuestionRepository.existsByQuestion_Id(id)).thenReturn(false);
        when(chapterRepository.findById(1)).thenReturn(Optional.of(chapter1));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuestionRequest updateRequest = QuestionRequest.builder()
                .chapterId(1)
                .content("Đổi sang câu hỏi tự luận")
                .difficulty(Difficulty.MEDIUM)
                .type(QuestionType.ESSAY)
                .referenceAnswer("Đáp án tự luận mới")
                .answers(Collections.emptyList())
                .build();

        QuestionResponse response = questionService.updateQuestion(id, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(QuestionType.ESSAY);
        assertThat(response.getReferenceAnswer()).isEqualTo("Đáp án tự luận mới");
        assertThat(response.getAnswers()).isEmpty();
        assertThat(existingQuestion.getAnswers()).isEmpty();
    }
}
