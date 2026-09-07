package com.exammatrix.backend.dto.request;

import com.exammatrix.backend.entity.enums.Difficulty;
import com.exammatrix.backend.entity.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {
    @NotNull(message = "Chương không được để trống")
    @Min(value = 1, message = "ID chương không hợp lệ")
    private Integer chapterId;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @Size(max = 3000, message = "Nội dung câu hỏi không được vượt quá 3000 ký tự")
    private String content;

    @NotNull(message = "Độ khó không được để trống")
    private Difficulty difficulty;

    @NotNull(message = "Loại câu hỏi không được để trống")
    private QuestionType type;

    @NotNull(message = "Danh sách đáp án không được để trống")
    @Size(min = 2, message = "Câu hỏi phải có ít nhất 2 đáp án")
    @Valid
    private List<AnswerRequest> answers;
}