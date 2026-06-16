package com.campuslearning.server.repository;

import com.campuslearning.server.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByClassIdOrderByCreatedAtDesc(Long classId);

    List<Assignment> findByClassId(Long classId);

    void deleteByClassId(Long classId);
}
