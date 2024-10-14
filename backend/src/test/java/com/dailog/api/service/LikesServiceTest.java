package com.dailog.api.service;

import static com.dailog.api.domain.enums.Role.MEMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dailog.api.domain.Likes;
import com.dailog.api.domain.Member;
import com.dailog.api.domain.Post;
import com.dailog.api.exception.likes.AlreadyLikedPost;
import com.dailog.api.repository.LikesRepository;
import com.dailog.api.repository.member.MemberRepository;
import com.dailog.api.repository.post.PostRepository;
import com.dailog.api.response.PagingResponse;
import com.dailog.api.response.likes.LikesMemberResponse;
import com.dailog.api.response.likes.LikesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class LikesServiceTest {

    @Autowired
    private LikesService likesService;
    @Autowired
    private LikesRepository likesRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        likesRepository.deleteAll();
        postRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글에 좋아요를 누른다.")
    void should_LikePost_When_LoggedIn() {
        //given
        Member member = getMember();
        Post post = getPost(member);

        //when
        likesService.like(member.getEmail(), post.getId());

        //then
        assertEquals(1L, likesRepository.count());
    }

    @Test
    @DisplayName("게시글에 좋아요는 1번만 누를 수 있다.")
    void should_NotAllowDuplicateLike_When_SamePost() {
        //given
        Member member = getMember();
        Post post = getPost(member);

        Likes likes = Likes.builder()
                .member(member)
                .post(post)
                .build();
        likesRepository.save(likes);

        //expected
        assertThrows(AlreadyLikedPost.class, () ->
                likesService.like(member.getEmail(), post.getId()));
    }

    @Test
    @DisplayName("이미 누른 좋아요 취소")
    void should_CancelLike_When_AlreadyLiked() {
        //given
        Member member = getMember();
        Post post = getPost(member);

        Likes likes = Likes.builder()
                .member(member)
                .post(post)
                .build();
        likesRepository.save(likes);

        //when
        likesService.cancelLike(member.getEmail(), post.getId());

        //then
        assertEquals(0, likesRepository.count());
    }

    @Test
    @DisplayName("게시글의 좋아요 수를 조회한다.")
    void should_GetLikes_When_ValidRequest() {
        //given
        Member member = getMember();
        Post post = getPost(member);

        Likes likes = Likes.builder()
                .member(member)
                .post(post)
                .build();
        likesRepository.save(likes);

        //when
        LikesResponse likesResponse = likesService.getCount(post.getId());

        //then
        assertEquals(1L, likesResponse.getLikes());
    }

    @Test
    @DisplayName("게시글에 좋아요를 누른 회원을 조회한다.")
    @Transactional
    void test() {
        //given
        Member member = getMember();
        Post post = getPost(member);
        for (int i = 1; i <= 10; i++) {
            Member tempMember = Member.builder()
                    .nickname(i + "번째 회원")
                    .email("member@test.com" + i)
                    .password("1234")
                    .role(MEMBER)
                    .build();
            memberRepository.save(tempMember);

            Likes likes = Likes.builder()
                    .member(tempMember)
                    .post(post)
                    .build();
            likesRepository.save(likes);
        }

        PageRequest pageRequest = PageRequest.of(0, 10);

        //when
        PagingResponse<LikesMemberResponse> members = likesService.getMembers(post.getId(), pageRequest);

        //then
        assertEquals(10L, members.getSize());
        assertEquals("10번째 회원", members.getItems().get(0).getNickname());
        assertEquals("1번째 회원", members.getItems().get(9).getNickname());
    }

    private Member getMember() {
        String encryptedPassword = passwordEncoder.encode("password123!");
        Member member = Member.builder()
                .name("member")
                .email("member@test.com")
                .password(encryptedPassword)
                .role(MEMBER)
                .build();
        memberRepository.save(member);
        return member;
    }

    private Post getPost(Member member) {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .member(member)
                .build();
        postRepository.save(post);
        return post;
    }
}