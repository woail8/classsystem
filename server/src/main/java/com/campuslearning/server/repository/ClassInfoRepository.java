package com.campuslearning.server.repository;

import com.campuslearning.server.model.ClassInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long> {

    Optional<ClassInfo> findByInviteCodeIgnoreCase(String inviteCode);

    List<ClassInfo> findByCourseId(Long courseId);

    long countByCourseId(Long courseId);
}
