package com.exammatrix.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "exam_papers")
public class ExamPaper {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matrix_id", nullable = false)
    private ExamMatrix examMatrix;

    @Column(name = "exam_code", length = 10, nullable = false)
    private String examCode;

    @Builder.Default
    @OneToMany(
            mappedBy = "examPaper",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PaperQuestion> paperQuestions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void addPaperQuestion(PaperQuestion paperQuestion) {
        paperQuestions.add(paperQuestion);
        paperQuestion.setExamPaper(this);
    }
}