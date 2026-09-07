package com.exammatrix.backend.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerResponse {
    private UUID id;
    private String content;
    private Boolean isCorrect;
}