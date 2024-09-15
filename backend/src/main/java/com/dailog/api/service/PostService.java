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
import com.dailog.api.response.post.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

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

    public PostResponse get(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(PostNotFound::new);

        return PostResponse.builder()
                .id(post.getId())
                .memberId(post.getMemberId())
                .nickname(post.getMember().getNickname())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .createdBy(post.getCreatedBy())
                .build();
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

    public PostResponse getPrevPost(long id) {
        Post prevPost = postRepository.findPrevPost(id)
                .orElseThrow(PostNotFound::new);

        return PostResponse.builder()
                .id(prevPost.getId())
                .title(prevPost.getTitle())
                .content(prevPost.getContent())
                .createdAt(prevPost.getCreatedAt())
                .createdBy(prevPost.getCreatedBy())
                .build();
    }

    public PostResponse getNextPost(long id) {
        Post prevPost = postRepository.findNextPost(id)
                .orElseThrow(PostNotFound::new);

        return PostResponse.builder()
                .id(prevPost.getId())
                .title(prevPost.getTitle())
                .content(prevPost.getContent())
                .createdAt(prevPost.getCreatedAt())
                .createdBy(prevPost.getCreatedBy())
                .build();
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
