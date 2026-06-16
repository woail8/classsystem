package com.campuslearning.server.repository;

import com.campuslearning.server.model.ClassMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassMemberRepository extends JpaRepository<ClassMember, Long> {

    List<ClassMember> findByUserId(Long userId);

    List<ClassMember> findByClassId(Long classId);

    Optional<ClassMember> findByClassIdAndUserId(Long classId, Long userId);

    boolean existsByClassIdAndUserId(Long classId, Long userId);

    void deleteByClassId(Long classId);

    void deleteByClassIdAndUserId(Long classId, Long userId);

    void deleteByClassIdInAndUserId(List<Long> classIds, Long userId);
}
