package com.dailog.api.service;

import com.dailog.api.domain.Member;
import com.dailog.api.domain.Post;
import com.dailog.api.domain.PostEditor;
import com.dailog.api.domain.PostEditor.PostEditorBuilder;
import com.dailog.api.exception.member.MemberNotFound;
import com.dailog.api.exception.post.ForbiddenPostAccess;
import com.dailog.api.exception.post.PostNotFound;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.repository.post.PostRepository;
import com.dailog.api.request.post.PostCreate;
import com.dailog.api.request.post.PostEdit;
import com.dailog.api.request.post.PostPageRequest;
import com.dailog.api.request.post.PostSearch;
import com.dailog.api.response.PagingResponse;
import com.dailog.api.response.post.PostIdResponse;
import com.dailog.api.response.post.PostResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void write(PostCreate postCreate, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFound::new);

        Post post = Post.builder()
                .member(member)
                .title(postCreate.getTitle())
                .content(postCreate.getContent())
                .build();

        postRepository.save(post);
    }

    public PostResponse get(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        return new PostResponse(post);
    }

    @Transactional
    public void viewPost(Long postId, String userIdentifier) {
        String key = "post:viewed:" + postId + ":" + userIdentifier;

        Boolean hasViewed = redisTemplate.hasKey(key);
        if (Boolean.FALSE.equals(hasViewed)) {
            increaseViews(postId);
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(key, "viewed", Duration.ofHours(24));
        }
    }

    @Transactional
    public void increaseViews(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);
        post.updateViews();
    }

    //글이 너무 많은 경우 비용이 너무 많이 든다. -> page와 size를 request로 받아 조회
    public PagingResponse<PostResponse> getList(PostPageRequest postPageRequest) {
        Page<Post> postPage = postRepository.getList(postPageRequest);

        return new PagingResponse<>(postPage, PostResponse.class);
    }

    //게시글 검색 결과 조회
    public PagingResponse<PostResponse> getList(PostSearch postSearch, PostPageRequest postPageRequest) {
        Page<Post> postPage = postRepository.getList(postSearch, postPageRequest);

        return new PagingResponse<>(postPage, PostResponse.class);
    }

    //public PostResponse getPrevPost(long id) {
    //    Post prevPost = postRepository.findPrevPost(id)
    //            .orElseThrow(PostNotFound::new);
    //
    //    return PostResponse.builder()
    //            .id(prevPost.getId())
    //            .title(prevPost.getTitle())
    //            .content(prevPost.getContent())
    //            .createdAt(prevPost.getCreatedAt())
    //            .createdBy(prevPost.getCreatedBy())
    //            .build();
    //}
    //
    //public PostResponse getNextPost(long id) {
    //    Post prevPost = postRepository.findNextPost(id)
    //            .orElseThrow(PostNotFound::new);
    //
    //    return PostResponse.builder()
    //            .id(prevPost.getId())
    //            .title(prevPost.getTitle())
    //            .content(prevPost.getContent())
    //            .createdAt(prevPost.getCreatedAt())
    //            .createdBy(prevPost.getCreatedBy())
    //            .build();
    //}

    public PostIdResponse getPrevPostId(Long postId) {
        Long prevPostId = postRepository.findPrevPostId(postId);
        if (prevPostId == null) {
            throw new PostNotFound();
        }
        return new PostIdResponse(prevPostId);
    }

    public PostIdResponse getNextPostId(Long postId) {
        Long nextPostId = postRepository.findNextPostId(postId);
        if (nextPostId == null) {
            throw new PostNotFound();
        }
        return new PostIdResponse(nextPostId);
    }

    @Transactional
    public void edit(Long postId, PostEdit request, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        Long memberId = post.getMemberId();
        validateWriter(memberId, email);

        PostEditorBuilder postEditorBuilder = post.toEditor();

        if (request.getTitle() != null) {
            postEditorBuilder.title(request.getTitle());
        }
        if (request.getContent() != null) {
            postEditorBuilder.content(request.getContent());
        }

        PostEditor postEditor = postEditorBuilder.build();

        post.edit(postEditor);
    }

    public void delete(Long postId, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        Long memberId = post.getMemberId();
        validateWriter(memberId, email);

        postRepository.delete(post);
    }

    private void validateWriter(Long memberId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFound::new);

        boolean isNotWriter = !member.getId().equals(memberId);
        if (isNotWriter) {
            throw new ForbiddenPostAccess();
        }
    }
}
