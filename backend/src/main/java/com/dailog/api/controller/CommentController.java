package com.dailog.api.controller;

import com.dailog.api.config.CustomUserDetails;
import com.dailog.api.exception.auth.Unauthorized;
import com.dailog.api.request.comment.CommentCreateForAnonymous;
import com.dailog.api.request.comment.CommentCreateForMember;
import com.dailog.api.request.comment.CommentDelete;
import com.dailog.api.request.comment.CommentEditForAnonymous;
import com.dailog.api.request.comment.CommentEditForMember;
import com.dailog.api.request.oAuth2.CustomOAuth2User;
import com.dailog.api.response.comment.CommentResponse;
import com.dailog.api.service.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public List<CommentResponse> get(@PathVariable Long postId) {
        return commentService.get(postId);
    }

    @PostMapping("/api/posts/{postId}/comments/member")
    public void writeByMember(@PathVariable Long postId,
                              @RequestBody @Validated CommentCreateForMember request,
                              @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            commentService.writeByMember(postId, request, username);
        }
    }

    @PostMapping("/api/posts/{postId}/comments/anonymous")
    public void writeByAnonymous(@PathVariable Long postId,
                                 @RequestBody @Validated CommentCreateForAnonymous request) {
        commentService.writeByAnonymous(postId, request);
    }

    //@PreAuthorize("hasRole('ROLE_MEMBER')")
    @PatchMapping("/api/comments/{commentId}/member")
    public void editByMember(@PathVariable Long commentId,
                             @RequestBody @Validated CommentEditForMember request,
                             @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            commentService.editByMember(commentId, request, username);
        }
    }

    @PatchMapping("/api/comments/{commentId}/anonymous")
    public void editMyAnonymous(@PathVariable Long commentId,
                                @RequestBody @Validated CommentEditForAnonymous request) {
        commentService.editByAnonymous(commentId, request);
    }

    //@DeleteMapping은 Body 요청 X
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/api/admin/comments/{commentId}/delete")
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }

    @PostMapping("/api/comments/{commentId}/delete/member")
    public void deleteMemberComment(@PathVariable Long commentId,
                                    @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            commentService.deleteMemberComment(commentId, username);
        }
    }

    @PostMapping("/api/comments/{commentId}/delete/anonymous")
    public void deleteAnonymousComment(@PathVariable Long commentId,
                                       @RequestBody CommentDelete request) {
        commentService.deleteAnonymousComment(commentId, request);
    }

    private String getUsernameFromPrincipal(Object principal) {
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUsername();
        } else if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getUsername();
        }
        throw new Unauthorized();
    }
}
