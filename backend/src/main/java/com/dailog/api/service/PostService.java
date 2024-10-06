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
import com.dailog.api.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTUtil jwtUtil;

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
        return new PostResponse(post, getViews(postId));
    }

    @Transactional
    public void viewPost(Long postId, HttpServletRequest request) {
        String key = "post:viewed:" + postId + ":" + getUserId(request);

        //key가 없으면 값을 설정하고 true 반환, key가 존재하면 값을 변경하지 않고 false 반환
        Boolean isNotViewed = redisTemplate.opsForValue().setIfAbsent(key, "Viewed", Duration.ofHours(24));

        //24시간 내에 조회한 적이 없을 경우 조회수 증가
        if (Boolean.TRUE.equals(isNotViewed)) {
            //조회수의 동시성 제어를 위해 비관적 락을 걸어 조회
            //Post post = postRepository.findByIdWithLock(postId)
            //        .orElseThrow(PostNotFound::new);
            //post.increaseViews();
            //postRepository.increaseViews(postId);
            incrementViews(postId);
        }
    }

    public void incrementViews(Long postId) {
        String key = "post:views:" + postId;
        redisTemplate.opsForValue().increment(key);
    }

    public int getViews(Long postId) {
        String key = "post:views:" + postId;
        Object currentViews = redisTemplate.opsForValue().get(key);

        if (currentViews == null) {
            Post post = postRepository.findById(postId)
                    .orElseThrow(PostNotFound::new);
            int dbViews = post.getViews();
            redisTemplate.opsForValue().set(key, dbViews);
            return dbViews;
        }

        return  (Integer) currentViews;
    }

    //@Scheduled(cron = "0 0 5 * * ?")  //오전 5시에 실행
    @Scheduled(cron = "0 */10 * * * ?")  //10분마다 실행
    @Transactional
    public void updateViewsToDatabase() {
        log.info("Scheduled updateViewsToDatabase");
        Set<String> keys = redisTemplate.keys("post:views:*");

        if (keys != null) {
            for (String key : keys) {
                Long postId = Long.parseLong(key.split(":")[2]);
                Integer views = (Integer) redisTemplate.opsForValue().get(key);

                if (views != null) {
                    Post post = postRepository.findById(postId)
                            .orElseThrow(PostNotFound::new);
                    post.updateViews(views);
                }
            }
            redisTemplate.delete(keys);
        }
    }

    //글이 너무 많은 경우 비용이 너무 많이 든다. -> page와 size를 request로 받아 조회
    public PagingResponse<PostResponse> getList(PostPageRequest postPageRequest) {
        Page<Post> postPage = postRepository.getList(postPageRequest);

        return new PagingResponse<>(postPage, PostResponse.class);
    }

    //게시글 검색 결과 조회
    public PagingResponse<PostResponse> getList(PostSearch postSearch, PostPageRequest postPageRequest) {
        Page<Post> postPage = postRepository.getList(postSearch, postPageRequest);

        for (Post post : postPage.getContent()) {
            post.updateViews(getViews(post.getId()));
        }

        return new PagingResponse<>(postPage, PostResponse.class);
    }

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

    private String getUserId(HttpServletRequest request) {
        String userIdentifier;

        String header = request.getHeader("Authorization");
        //로그인된 사용자는 이메일을 사용
        if (header != null) {
            String access = header.substring("Bearer".length()).trim();
            userIdentifier = "user:" + (long) jwtUtil.getUsername(access).hashCode();
        }
        //비회원은 IP 주소와 User-Agent를 사용
        else {
            String ipAddress = request.getRemoteAddr();

            String userAgent = request.getHeader("User-Agent");
            if (userAgent == null || userAgent.isEmpty()) {
                userIdentifier = "guest:" + ipAddress.hashCode();
            } else {
                String identifier = ipAddress + userAgent;
                userIdentifier = "guest:" + (long) identifier.hashCode();
            }
        }

        return userIdentifier;
    }
}
