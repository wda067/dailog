package com.dailog.api.controller;

import com.dailog.api.config.CustomUserDetails;
import com.dailog.api.exception.auth.Unauthorized;
import com.dailog.api.request.oAuth2.CustomOAuth2User;
import com.dailog.api.request.post.PostCreate;
import com.dailog.api.request.post.PostEdit;
import com.dailog.api.request.post.PostPageRequest;
import com.dailog.api.request.post.PostSearch;
import com.dailog.api.response.PagingResponse;
import com.dailog.api.response.post.PostIdResponse;
import com.dailog.api.response.post.PostResponse;
import com.dailog.api.service.PostService;
import com.dailog.api.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * /posts -> 글 전체 조회(검색 + 페이징) /posts/{postId} -> 글 한 개만 조회
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class PostController {

    //SSR -> jsp, thymeleaf, mustache
    //    -> html rendering
    //SPA -> vue.js + SSR = nuxt.js
    //       react + SSR = next.js
    //    -> javascript <-> API (JSON)

    //@PostMapping("/posts")
    //public String post(@RequestParam String title, @RequestParam String content) {
    //    log.info("title: {}, content: {}", title, content);
    //    return "Hello World";
    //}
    //
    //@PostMapping("/posts")
    //public String post(@RequestParam Map<String, String> params) {
    //    log.info("params={}", params);
    //    return "Hello World";
    //}
    //
    //@PostMapping("/posts")
    //public String post(PostCreate params) {
    //    log.info("params={}", params.toString());
    //    return "Hello World";
    //}

    //1. 저장한 데이터 Entity -> response로 응답
    //2. 저장한 데이터의 primary_id -> response로 응답
    //3. 응답 필요 없음 -> client에서 모든 데이터를 관리

    private final PostService postService;
    private final JWTUtil jWTUtil;

    @PostMapping("/api/posts")
    public void post(@RequestBody @Validated PostCreate request,
                     @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            postService.write(request, username);
        }
    }

    @PatchMapping("/api/posts/{postId}")
    public void edit(@PathVariable Long postId, @RequestBody @Validated PostEdit request,
                     @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            postService.edit(postId, request, username);
        }
    }

    @DeleteMapping("/api/posts/{postId}")
    public void delete(@PathVariable Long postId,
                       @AuthenticationPrincipal Object principal) {
        String username = getUsernameFromPrincipal(principal);
        if (username != null) {
            postService.delete(postId, username);
        }
    }

    @GetMapping("/api/posts/{postId}")
    public PostResponse get(@PathVariable Long postId, HttpServletRequest request) {
        postService.viewPost(postId, request);
        return postService.get(postId);
    }

    //@GetMapping("/api/posts/{postId}/prev")
    //public PostResponse getPrevPost(@PathVariable Long postId) {
    //    return postService.getPrevPost(postId);
    //}
    //
    //@GetMapping("/api/posts/{postId}/next")
    //public PostResponse getNextPost(@PathVariable Long postId) {
    //    return postService.getNextPost(postId);
    //}

    @GetMapping("/api/posts/{postId}/prev")
    public PostIdResponse getPrevPostId(@PathVariable Long postId) {
        return postService.getPrevPostId(postId);
    }

    @GetMapping("/api/posts/{postId}/next")
    public PostIdResponse getNextPostId(@PathVariable Long postId) {
        return postService.getNextPostId(postId);
    }

    @GetMapping("/api/posts")
    public PagingResponse<PostResponse> getList(PostPageRequest postPageRequest) {
        return postService.getList(postPageRequest);
    }

    @GetMapping(value = "/api/posts", params = {"searchDateType", "searchType", "searchQuery"})
    public PagingResponse<PostResponse> getSearchList(PostSearch postSearch, PostPageRequest postPageRequest) {
        return postService.getList(postSearch, postPageRequest);
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
