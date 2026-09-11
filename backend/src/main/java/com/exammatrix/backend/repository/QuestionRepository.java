package com.exammatrix.backend.repository;

import com.exammatrix.backend.entity.Question;
import com.exammatrix.backend.entity.enums.Difficulty;
import com.exammatrix.backend.entity.enums.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    @Query("""
        SELECT q
        FROM Question q
        JOIN q.chapter c
        JOIN c.subject s
        WHERE (
            :search IS NULL
            OR LOWER(q.content) LIKE LOWER(
                CONCAT('%', CAST(:search AS string), '%')
            )
        )
        AND (
            :subjectId IS NULL
            OR s.id = :subjectId
        )
        AND (
            :chapterId IS NULL
            OR c.id = :chapterId
        )
        AND (
            :difficulty IS NULL
            OR q.difficulty = :difficulty
        )
        AND (
            :type IS NULL
            OR q.type = :type
        )
    """)
    Page<Question> searchQuestions(
        @Param("search") String search,
        @Param("subjectId") Integer subjectId,
        @Param("chapterId") Integer chapterId,
        @Param("difficulty") Difficulty difficulty,
        @Param("type") QuestionType type,
        Pageable pageable
    );

    long countByChapter_IdAndDifficulty(Integer chapterId, Difficulty difficulty);

    @Override
    @EntityGraph(attributePaths = {
        "chapter",
        "teacher",
        "answers"
    })
    Optional<Question> findById(UUID id);

    @EntityGraph(attributePaths = "answers")
    List<Question> findAllByChapter_IdAndDifficulty(
            Integer chapterId,
            Difficulty difficulty
    );

    long countByDifficulty(Difficulty difficulty);
}