package com.campuslearning.server.repository;

import com.campuslearning.server.model.SignInTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SignInTaskRepository extends JpaRepository<SignInTask, Long> {

    Optional<SignInTask> findFirstByClassIdAndDeadlineAfterOrderByCreatedAtDesc(Long classId, LocalDateTime now);

    List<SignInTask> findByClassId(Long classId);

    void deleteByClassId(Long classId);
}
