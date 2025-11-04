package com.planti.domain.userplant.repository;

import com.planti.domain.userplant.entity.UserPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPlantRepository extends JpaRepository<UserPlant, Integer> {
    Optional<UserPlant> findByUser_UserId(Long userId);
}