package com.dailog.api.service;

import com.dailog.api.domain.Likes;
import com.dailog.api.domain.Member;
import com.dailog.api.domain.Post;
import com.dailog.api.exception.likes.AlreadyLikedPost;
import com.dailog.api.exception.likes.LikesNotFound;
import com.dailog.api.exception.member.MemberNotFound;
import com.dailog.api.exception.post.PostNotFound;
import com.dailog.api.repository.LikesRepository;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.repository.post.PostRepository;
import com.dailog.api.response.PagingResponse;
import com.dailog.api.response.likes.LikesMemberResponse;
import com.dailog.api.response.likes.LikesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikesService {

    private final LikesRepository likesRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public void like(String email, Long postId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFound::new);
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFound::new);

        boolean alreadyLiked = likesRepository.existsByMemberIdAndPostId(member.getId(), postId);
        if (alreadyLiked) {
            throw new AlreadyLikedPost();
        }

        Likes likes = Likes.builder()
                .member(member)
                .post(post)
                .build();
        likesRepository.save(likes);
    }

    public void cancelLike(String email, Long postId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFound::new);
        Likes likes = likesRepository.findByMemberIdAndPostId(member.getId(), postId)
                .orElseThrow(LikesNotFound::new);

        likesRepository.delete(likes);
    }

    public LikesResponse getCount(Long postId) {
        long likes = likesRepository.countByPostId(postId);
        return LikesResponse.builder()
                .likes(likes)
                .build();
    }

    public LikesResponse getStatus(String email, Long postId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFound::new);
        boolean alreadyLiked = likesRepository.existsByMemberIdAndPostId(member.getId(), postId);

        return LikesResponse.builder()
                .likedStatus(alreadyLiked)
                .build();
    }

    public PagingResponse<LikesMemberResponse> getMembers(Long postId, Pageable pageable) {
        Page<LikesMemberResponse> pageMember = likesRepository.findMembersByPostId(postId, pageable);

        return new PagingResponse<>(pageMember, LikesMemberResponse.class);
    }
}
