package com.planti.domain.community.repository;

import com.planti.domain.community.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Integer> {
}