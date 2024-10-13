package com.dailog.api.controller;

import com.dailog.api.config.CustomUserDetails;
import com.dailog.api.exception.auth.Unauthorized;
import com.dailog.api.request.oAuth2.CustomOAuth2User;
import com.dailog.api.response.likes.LikesResponse;
import com.dailog.api.service.LikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikesController {

    private final LikesService likesService;

    @GetMapping("/api/posts/{postId}/likes")
    public LikesResponse getCount(@PathVariable Long postId) {
        return likesService.getCount(postId);
    }

    @GetMapping("/api/posts/{postId}/likes/status")
    public LikesResponse getLikeStatus(@PathVariable Long postId,
                                       @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            return likesService.getStatus(username, postId);
        } else {
            throw new Unauthorized();
        }
    }

    @PostMapping("/api/posts/{postId}/likes")
    public void like(@PathVariable Long postId,
                     @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            likesService.like(username, postId);
        }
    }

    @DeleteMapping("/api/posts/{postId}/likes")
    public void cancel(@PathVariable Long postId,
                     @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            likesService.cancelLike(username, postId);
        }
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
