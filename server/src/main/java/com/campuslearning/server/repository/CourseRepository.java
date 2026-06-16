package com.campuslearning.server.repository;

import com.campuslearning.server.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByInviteCodeIgnoreCase(String inviteCode);
}
