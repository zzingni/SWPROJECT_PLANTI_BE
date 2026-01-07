package com.planti.domain.environment.repository;

import com.planti.domain.environment.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
}
