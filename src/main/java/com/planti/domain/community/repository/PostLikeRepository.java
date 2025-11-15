package com.planti.domain.community.repository;

import com.planti.domain.community.entity.Post;
import com.planti.domain.community.entity.PostLike;
import com.planti.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 존재 여부 확인
    boolean existsByUserAndPost(User user, Post post);

    // 존재 여부 확인 (userId, postId로)
    @Query("SELECT CASE WHEN COUNT(pl) > 0 THEN true ELSE false END FROM PostLike pl WHERE pl.user.userId = :userId AND pl.post.postId = :postId")
    boolean existsByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    // 조회
    Optional<PostLike> findByUserAndPost(User user, Post post);

    // 삭제
    void deleteByUserAndPost(User user, Post post);

    // 특정 사용자가 좋아요한 게시글 ID 리스트 조회
    @Query("SELECT pl.post.postId FROM PostLike pl WHERE pl.user.userId = :userId AND pl.post.postId IN :postIds")
    List<Long> findPostIdsLikedByUser(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    // postId 리스트에 대한 좋아요 수 집계 (각 row: Object[]{postId(Long), count(Long)})
    @Query("SELECT pl.post.postId, COUNT(pl) FROM PostLike pl WHERE pl.post.postId IN :postIds GROUP BY pl.post.postId")
    List<Object[]> countLikesByPostIds(@Param("postIds") List<Long> postIds);

    // 단일 postId 카운트
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.postId = :postId")
    Long countByPostId(@Param("postId") Long postId);
}