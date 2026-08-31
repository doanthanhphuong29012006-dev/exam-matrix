package com.exammatrix.backend.repository;

import com.exammatrix.backend.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
    List<Chapter> findAllBySubjectIdOrderByOrderIndexAsc(
            Integer subjectId
    );

    boolean existsBySubjectIdAndOrderIndex(
            Integer subjectId,
            Integer orderIndex
    );

    boolean existsBySubjectIdAndOrderIndexAndIdNot(
            Integer subjectId,
            Integer orderIndex,
            Integer id
    );
}