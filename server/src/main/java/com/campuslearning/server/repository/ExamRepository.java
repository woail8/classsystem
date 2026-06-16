package com.campuslearning.server.repository;

import com.campuslearning.server.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByClassIdOrderByPublishedAtDesc(Long classId);

    void deleteByClassId(Long classId);
}
