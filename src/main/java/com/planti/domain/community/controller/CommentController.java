package com.planti.domain.community.controller;

import com.planti.domain.community.dto.request.CommentCreateRequest;
import com.planti.domain.community.dto.request.CommentUpdateRequest;
import com.planti.domain.community.dto.response.CommentDto;
import com.planti.domain.community.service.PostService;
import com.planti.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final PostService postService;

    // 댓글 작성
    @PostMapping
    public CommentDto createComment(
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return postService.createComment(request.getPostId(), currentUser.getUserId(), request.getContent());
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public CommentDto updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return postService.updateComment(commentId, currentUser.getUserId(), request.getContent());
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public void deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser
    ) {
        postService.deleteComment(commentId, currentUser.getUserId());
    }
}