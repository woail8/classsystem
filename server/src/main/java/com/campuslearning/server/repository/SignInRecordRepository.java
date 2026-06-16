package com.campuslearning.server.repository;

import com.campuslearning.server.model.SignInRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignInRecordRepository extends JpaRepository<SignInRecord, Long> {

    boolean existsByTaskIdAndStudentId(Long taskId, Long studentId);

    List<SignInRecord> findByTaskId(Long taskId);

    void deleteByTaskIdIn(List<Long> taskIds);

    void deleteByClassId(Long classId);
}
