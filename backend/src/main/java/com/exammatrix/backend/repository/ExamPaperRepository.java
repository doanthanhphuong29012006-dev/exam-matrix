package com.exammatrix.backend.repository;

import com.exammatrix.backend.entity.ExamPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamPaperRepository extends JpaRepository<ExamPaper, UUID> {
    @Query("""
        SELECT p
        FROM ExamPaper p
        JOIN p.examMatrix m
        JOIN m.teacher t
        WHERE (
            :matrixId IS NULL
            OR m.id = :matrixId
        )
        AND (
            :teacherId IS NULL
            OR t.id = :teacherId
        )
        """)
    Page<ExamPaper> searchExamPapers(
            @Param("matrixId") UUID matrixId,
            @Param("teacherId") UUID teacherId,
            Pageable pageable
    );

    boolean existsByExamMatrix_IdAndExamCode(
            UUID matrixId,
            String examCode
    );

    @Override
    @EntityGraph(attributePaths = {
            "examMatrix",
            "examMatrix.teacher",
            "paperQuestions",
            "paperQuestions.question"
    })
    Optional<ExamPaper> findById(UUID id);
}