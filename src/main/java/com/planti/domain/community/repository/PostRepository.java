package com.planti.domain.community.repository;

import com.planti.domain.community.entity.Post;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 페이징 없는 게시글 조회 (active 상태만)
    List<Post> findByBoard_BoardIdAndStatus(Integer boardId, String status);

    // 페이징 포함
    Page<Post> findByBoard_BoardIdAndStatus(Integer boardId, String status, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.comments WHERE p.postId = :id")
    Post findByIdWithComments(@Param("id") Long id);
}