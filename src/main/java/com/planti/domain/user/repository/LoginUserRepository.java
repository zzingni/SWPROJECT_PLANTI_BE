package com.planti.domain.user.repository;

import com.planti.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginUserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLoginId(String loginId);
}