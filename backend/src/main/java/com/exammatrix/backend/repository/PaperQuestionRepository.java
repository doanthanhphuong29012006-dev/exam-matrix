package com.exammatrix.backend.repository;

import com.exammatrix.backend.entity.PaperQuestion;
import com.exammatrix.backend.entity.id.PaperQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperQuestionRepository extends JpaRepository<PaperQuestion, PaperQuestionId> {
}