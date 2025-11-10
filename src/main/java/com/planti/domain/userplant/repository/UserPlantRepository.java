package com.planti.domain.userplant.repository;

import com.planti.domain.userplant.entity.UserPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPlantRepository extends JpaRepository<UserPlant, Integer> {
    Optional<UserPlant> findByUser_UserId(Long userId);

    @Query("SELECT up FROM UserPlant up JOIN FETCH up.user u WHERE u.userId = :userId")
    List<UserPlant> findAllWithUser(@Param("userId") Long userId);
}