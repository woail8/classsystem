package com.campuslearning.server.repository;

import com.campuslearning.server.model.NotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, Long> {

    List<NotificationMessage> findByClassIdInOrderByCreatedAtDesc(List<Long> classIds);

    void deleteByClassId(Long classId);
}
