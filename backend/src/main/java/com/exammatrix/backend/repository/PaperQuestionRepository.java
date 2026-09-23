package com.exammatrix.backend.repository;

import com.exammatrix.backend.entity.PaperQuestion;
import com.exammatrix.backend.entity.id.PaperQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaperQuestionRepository extends JpaRepository<PaperQuestion, PaperQuestionId> {
    boolean existsByQuestion_Id(UUID questionId);
}