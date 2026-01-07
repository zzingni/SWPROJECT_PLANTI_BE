package com.planti.domain.environment.repository;

import com.planti.domain.environment.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
    Optional<Environment> findTopByUser_UserIdOrderByRecordedAtDesc(Long userId);
}
