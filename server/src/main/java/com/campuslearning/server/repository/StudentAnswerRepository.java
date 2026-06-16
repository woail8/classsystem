package com.campuslearning.server.repository;

import com.campuslearning.server.model.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    List<StudentAnswer> findByRecordIdOrderByQuestionIdAsc(Long recordId);

    void deleteByRecordId(Long recordId);

    void deleteByRecordIdIn(List<Long> recordIds);
}
