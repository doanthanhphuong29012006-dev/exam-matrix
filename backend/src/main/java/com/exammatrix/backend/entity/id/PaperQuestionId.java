package com.exammatrix.backend.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class PaperQuestionId implements Serializable {
    @Column(name = "exam_paper_id")
    private UUID examPaperId;

    @Column(name = "question_id")
    private UUID questionId;
}