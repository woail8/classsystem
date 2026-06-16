package com.campuslearning.server.repository;

import com.campuslearning.server.model.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    List<ExamQuestion> findByExamIdOrderBySortOrderAsc(Long examId);

    void deleteByExamId(Long examId);

    void deleteByExamIdIn(List<Long> examIds);
}
