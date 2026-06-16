package com.campuslearning.server.repository;

import com.campuslearning.server.model.ResourceFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceFileRepository extends JpaRepository<ResourceFile, Long> {

    List<ResourceFile> findByClassIdOrderByUploadTimeDesc(Long classId);

    void deleteByClassId(Long classId);
}
