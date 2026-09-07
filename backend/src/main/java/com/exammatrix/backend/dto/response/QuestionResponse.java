package com.exammatrix.backend.dto.response;

import com.exammatrix.backend.entity.enums.Difficulty;
import com.exammatrix.backend.entity.enums.QuestionType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private UUID id;
    private Integer chapterId;
    private String content;
    private UUID teacherId;
    private Difficulty difficulty;
    private QuestionType type;
    private List<AnswerResponse> answers;
    private LocalDateTime createdAt;
}