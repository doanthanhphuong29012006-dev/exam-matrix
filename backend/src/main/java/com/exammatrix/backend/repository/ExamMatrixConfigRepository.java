package com.exammatrix.backend.repository;

import com.exammatrix.backend.entity.ExamMatrixConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamMatrixConfigRepository extends JpaRepository<ExamMatrixConfig, Integer> {
}