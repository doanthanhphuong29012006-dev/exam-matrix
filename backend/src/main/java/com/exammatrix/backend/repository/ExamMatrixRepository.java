package com.exammatrix.backend.repository;

import com.exammatrix.backend.entity.ExamMatrix;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamMatrixRepository extends JpaRepository<ExamMatrix, UUID> {
    @Query("""
        SELECT m
        FROM ExamMatrix m
        JOIN m.subject s
        JOIN m.teacher t
        WHERE (
            :search IS NULL
            OR LOWER(m.title) LIKE LOWER(
                CONCAT('%', CAST(:search AS string), '%')
            )
        )
        AND (
            :subjectId IS NULL
            OR s.id = :subjectId
        )
        AND (
            :teacherId IS NULL
            OR t.id = :teacherId
        )
    """)
    Page<ExamMatrix> searchExamMatrices(
            @Param("search") String search,
            @Param("subjectId") Integer subjectId,
            @Param("teacherId") UUID teacherId,
            Pageable pageable
    );

    @Override
    @EntityGraph(attributePaths = {
            "teacher",
            "subject",
            "configs",
            "configs.chapter"
    })
    Optional<ExamMatrix> findById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT m
    FROM ExamMatrix m
    WHERE m.id = :id
    """)
    Optional<ExamMatrix> findByIdForUpdate(
            @Param("id") UUID id
    );

    long countByTeacher_Id(UUID teacherId);

    List<ExamMatrix> findTop5ByOrderByUpdatedAtDesc();

    List<ExamMatrix> findTop5ByTeacher_IdOrderByUpdatedAtDesc(
            UUID teacherId
    );
}