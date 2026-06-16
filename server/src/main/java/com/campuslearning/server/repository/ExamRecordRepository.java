package com.campuslearning.server.repository;

import com.campuslearning.server.model.ExamRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamRecordRepository extends JpaRepository<ExamRecord, Long> {

    Optional<ExamRecord> findByExamIdAndStudentId(Long examId, Long studentId);

    List<ExamRecord> findByExamIdOrderBySubmittedAtDesc(Long examId);

    void deleteByExamId(Long examId);

    void deleteByExamIdIn(List<Long> examIds);
}
