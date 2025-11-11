package com.planti.domain.community.repository;

import com.planti.domain.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByPostPostId(Integer postId);
}